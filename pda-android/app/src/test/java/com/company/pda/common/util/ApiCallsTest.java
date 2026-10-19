package com.company.pda.common.util;

import static org.junit.Assert.*;

import com.company.pda.common.exception.ApiException;
import com.company.pda.data.remote.api.*;
import com.company.pda.data.remote.dto.auth.AuthDto.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;

public class ApiCallsTest {
  /** Minimal socket fixture: Android unit-test compilation excludes JDK HttpServer. */
  private static class Headers extends TreeMap<String, String> {
    Headers() {
      super(String.CASE_INSENSITIVE_ORDER);
    }

    String getFirst(String name) {
      return get(name);
    }
  }

  private static class HttpExchange {
    final Socket socket;
    final String method;
    final URI uri;
    final Headers headers = new Headers();
    final byte[] body;

    HttpExchange(Socket socket) throws IOException {
      this.socket = socket;
      var input = new BufferedInputStream(socket.getInputStream());
      String[] request = line(input).split(" ");
      method = request[0];
      uri = URI.create(request[1]);
      String header;
      while (!(header = line(input)).isEmpty()) {
        int colon = header.indexOf(':');
        headers.put(header.substring(0, colon), header.substring(colon + 1).trim());
      }
      int length = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
      body = new byte[length];
      new DataInputStream(input).readFully(body);
    }

    private static String line(InputStream input) throws IOException {
      var bytes = new ByteArrayOutputStream();
      int next;
      while ((next = input.read()) != -1 && next != '\n') if (next != '\r') bytes.write(next);
      if (next == -1) throw new EOFException();
      return bytes.toString("UTF-8");
    }

    Headers getRequestHeaders() {
      return headers;
    }

    String getRequestMethod() {
      return method;
    }

    URI getRequestURI() {
      return uri;
    }

    byte[] getRequestBody() {
      return body;
    }

    OutputStream getResponseBody() throws IOException {
      return socket.getOutputStream();
    }

    void sendResponseHeaders(int status, long length) throws IOException {
      String headers =
          "HTTP/1.1 "
              + status
              + " Response\r\nContent-Length: "
              + Math.max(0, length)
              + "\r\nConnection: close\r\n\r\n";
      socket.getOutputStream().write(headers.getBytes(StandardCharsets.UTF_8));
    }
  }

  private static class HttpServer {
    interface Handler {
      void handle(HttpExchange exchange) throws IOException;
    }

    final ServerSocket socket;
    final Map<String, Handler> handlers = new ConcurrentHashMap<>();
    final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();
    ExecutorService executor;
    Thread acceptor;
    volatile boolean stopped;

    private HttpServer(InetSocketAddress address) throws IOException {
      socket = new ServerSocket();
      socket.bind(address);
    }

    static HttpServer create(InetSocketAddress address, int ignored) throws IOException {
      return new HttpServer(address);
    }

    InetSocketAddress getAddress() {
      return (InetSocketAddress) socket.getLocalSocketAddress();
    }

    void setExecutor(ExecutorService executor) {
      this.executor = executor;
    }

    void createContext(String path, Handler handler) {
      handlers.put(path, handler);
    }

    void start() {
      acceptor =
          new Thread(
              () -> {
                while (!stopped) {
                  try {
                    Socket client = socket.accept();
                    client.setSoTimeout(5000);
                    executor.execute(
                        () -> {
                          try (client) {
                            var exchange = new HttpExchange(client);
                            var handler =
                                handlers.entrySet().stream()
                                    .filter(
                                        entry -> exchange.uri.getPath().startsWith(entry.getKey()))
                                    .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                                    .orElseThrow(() -> new IOException("Unexpected endpoint"))
                                    .getValue();
                            handler.handle(exchange);
                          } catch (Throwable error) {
                            errors.add(error);
                          }
                        });
                  } catch (IOException error) {
                    if (!stopped) errors.add(error);
                  }
                }
              });
      acceptor.start();
    }

    void stop(int ignored) {
      stopped = true;
      try {
        socket.close();
        acceptor.join(5000);
      } catch (Exception error) {
        throw new AssertionError(error);
      }
      if (!errors.isEmpty()) throw new AssertionError(errors.peek());
    }
  }

  private HttpServer server;
  private ExecutorService serverThreads;
  private ApiCalls api;
  private Store store;
  private final AtomicInteger refreshes = new AtomicInteger();

  private static class Store implements ApiCalls.TokenStore {
    private Tokens tokens = ApiCallsTest.tokens("old", "refresh");

    public synchronized Tokens tokens() {
      return tokens;
    }

    public synchronized void tokens(Tokens value) {
      tokens = value;
    }

    public synchronized void logout() {
      tokens = null;
    }
  }

  private static Tokens tokens(String access, String refresh) {
    Tokens tokens = new Tokens();
    tokens.accessToken = access;
    tokens.refreshToken = refresh;
    return tokens;
  }

  @Before
  public void setup() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    serverThreads = Executors.newCachedThreadPool();
    server.setExecutor(serverThreads);
    server.start();
    store = new Store();
    api = new ApiCalls("http://127.0.0.1:" + server.getAddress().getPort() + "/", store);
  }

  @After
  public void cleanup() {
    server.stop(0);
    serverThreads.shutdownNow();
  }

  private static void respond(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, status == 204 ? -1 : bytes.length);
    try (var output = exchange.getResponseBody()) {
      if (status != 204) output.write(bytes);
    }
  }

  private void refreshEndpoint(int status) {
    server.createContext(
        "/auth/refresh",
        exchange -> {
          refreshes.incrementAndGet();
          assertNull(exchange.getRequestHeaders().getFirst("Authorization"));
          assertEquals("POST", exchange.getRequestMethod());
          assertEquals(
              "{\"refreshToken\":\"refresh\"}",
              new String(exchange.getRequestBody(), StandardCharsets.UTF_8));
          respond(
              exchange,
              status,
              status == 200 ? "{\"accessToken\":\"new\",\"refreshToken\":\"rotated\"}" : "{}");
        });
  }

  private static <T> T await(CompletableFuture<T> request) throws IOException {
    try {
      return request.join();
    } catch (CompletionException failure) {
      if (failure.getCause() instanceof IOException error) throw error;
      throw failure;
    }
  }

  @Test
  public void callApiReturnsBeforeResponseAndUsesWorkerThread() throws Exception {
    var received = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    server.createContext(
        "/resource",
        exchange -> {
          received.countDown();
          try {
            if (!release.await(5, TimeUnit.SECONDS)) throw new IOException("Test timed out");
          } catch (InterruptedException error) {
            throw new IOException(error);
          }
          respond(exchange, 200, "\"ok\"");
        });
    var workers = Executors.newSingleThreadExecutor();
    try {
      var result = api.<String>callApi("GET", "resource", null, String.class, Map.of(), workers);
      assertTrue(received.await(5, TimeUnit.SECONDS));
      assertFalse(result.isDone());
      release.countDown();
      assertEquals("ok", result.get(5, TimeUnit.SECONDS));
    } finally {
      release.countDown();
      workers.shutdownNow();
    }
  }

  @Test
  public void refreshesAndRetriesWithNewJwt() throws Exception {
    refreshEndpoint(200);
    var calls = new AtomicInteger();
    server.createContext(
        "/resource",
        exchange -> {
          calls.incrementAndGet();
          boolean renewed =
              "Bearer new".equals(exchange.getRequestHeaders().getFirst("Authorization"));
          respond(exchange, renewed ? 200 : 401, renewed ? "\"ok\"" : "{}");
        });
    assertEquals(
        "ok",
        await(api.<String>callApi("GET", "resource", null, String.class, Map.of(), Runnable::run)));
    assertEquals(2, calls.get());
    assertEquals(1, refreshes.get());
    assertEquals("rotated", store.tokens().refreshToken);
  }

  @Test
  public void expiredAccessRefreshesBeforeSendingProtectedRequest() throws Exception {
    store.tokens().accessExpiresAt = System.currentTimeMillis() - 1000;
    store.tokens().sessionExpiresAt = System.currentTimeMillis() + 60000;
    refreshEndpoint(200);
    server.createContext(
        "/resource",
        exchange -> {
          assertEquals("Bearer new", exchange.getRequestHeaders().getFirst("Authorization"));
          assertEquals("ANDROID", exchange.getRequestHeaders().getFirst("PDA_TYPE"));
          respond(exchange, 200, "\"ok\"");
        });
    assertEquals(
        "ok",
        await(api.<String>callApi("GET", "resource", null, String.class, Map.of(), Runnable::run)));
    assertEquals(1, refreshes.get());
  }

  @Test
  public void revokedSessionIsNotRefreshed() {
    refreshEndpoint(200);
    server.createContext(
        "/resource",
        exchange ->
            respond(exchange, 401, "{\"code\":\"SESSION_REVOKED\",\"detail\":\"Sign in again\"}"));
    ApiException error =
        assertThrows(
            ApiException.class,
            () ->
                await(api.callApi("GET", "resource", null, String.class, Map.of(), Runnable::run)));
    assertEquals("SESSION_REVOKED", error.code);
    assertEquals(0, refreshes.get());
    assertNull(store.tokens());
  }

  @Test
  public void expiredSessionDoesNotSendHttp() {
    store.tokens().sessionExpiresAt = System.currentTimeMillis() - 1000;
    ApiException error =
        assertThrows(
            ApiException.class,
            () ->
                await(api.callApi("GET", "resource", null, String.class, Map.of(), Runnable::run)));
    assertEquals("SESSION_EXPIRED", error.code);
    assertNull(store.tokens());
  }

  @Test
  public void explicitExtensionSendsOptInAndStoresRotatedTokens() {
    server.createContext(
        "/auth/refresh",
        exchange -> {
          assertEquals(
              "{\"refreshToken\":\"refresh\",\"extendSession\":true}",
              new String(exchange.getRequestBody(), StandardCharsets.UTF_8));
          respond(exchange, 200, "{\"accessToken\":\"new\",\"refreshToken\":\"rotated\"}");
        });
    assertEquals("rotated", api.extendSession(Runnable::run).join().refreshToken);
    assertEquals("rotated", store.tokens().refreshToken);
  }

  @Test
  public void retriesOnlyOnce() throws Exception {
    refreshEndpoint(200);
    var calls = new AtomicInteger();
    server.createContext(
        "/resource",
        exchange -> {
          calls.incrementAndGet();
          respond(exchange, 401, "{\"detail\":\"Denied\"}");
        });
    var error =
        assertThrows(
            ApiException.class,
            () ->
                await(api.callApi("GET", "resource", null, String.class, Map.of(), Runnable::run)));
    assertEquals("Denied", error.getMessage());
    assertNull(store.tokens());
    assertEquals(2, calls.get());
    assertEquals(1, refreshes.get());
  }

  @Test
  public void rejectedRefreshClearsSession() {
    refreshEndpoint(401);
    server.createContext("/resource", exchange -> respond(exchange, 401, "{}"));
    assertThrows(
        ApiException.class,
        () -> await(api.callApi("GET", "resource", null, String.class, Map.of(), Runnable::run)));
    assertNull(store.tokens());
  }

  @Test
  public void serverFailureDuringRefreshPreservesSession() {
    refreshEndpoint(503);
    server.createContext("/resource", exchange -> respond(exchange, 401, "{}"));
    assertEquals(
        503,
        assertThrows(
                ApiException.class,
                () ->
                    await(
                        api.callApi(
                            "GET", "resource", null, String.class, Map.of(), Runnable::run)))
            .status);
    assertEquals("old", store.tokens().accessToken);
  }

  @Test
  public void loginAndDeviceRequestsDoNotSendJwtOrRefresh() {
    refreshEndpoint(200);
    server.createContext(
        "/",
        exchange -> {
          assertNull(exchange.getRequestHeaders().getFirst("Authorization"));
          if (exchange.getRequestURI().getPath().equals("/pda/commands")) {
            assertEquals("42", exchange.getRequestHeaders().getFirst("X-Device-Id"));
            assertEquals("secret", exchange.getRequestHeaders().getFirst("X-Device-Secret"));
          }
          respond(exchange, 401, "{}");
        });
    assertThrows(
        ApiException.class,
        () -> await(new AuthApi(api, Runnable::run).login(new Login("user", "pass"))));
    assertThrows(
        ApiException.class,
        () -> await(new PdaFinderApi(api, Runnable::run).commands(42, "secret")));
    assertEquals(0, refreshes.get());
    assertNotNull(store.tokens());
  }

  @Test
  public void decodesListsAndEmptyResponses() throws Exception {
    server.createContext(
        "/pda/commands", exchange -> respond(exchange, 200, "[{\"command\":\"FIND\"}]"));
    server.createContext("/devices/token", exchange -> respond(exchange, 204, ""));
    var finder = new PdaFinderApi(api, Runnable::run);
    assertEquals("FIND", await(finder.commands(1, "secret")).get(0).command);
    assertNull(await(finder.token(1, "secret", null)));
  }

  @Test
  public void encodesBarcodeAsSinglePathSegment() throws Exception {
    server.createContext(
        "/products/barcode/",
        exchange -> {
          assertEquals("/products/barcode/a%2Fb%20%2B%3F", exchange.getRequestURI().getRawPath());
          respond(exchange, 200, "{}");
        });
    assertNotNull(await(new ProductApi(api, Runnable::run).product("a/b +?")));
  }

  @Test
  public void concurrentUnauthorizedRequestsShareOneRotatingRefresh() throws Exception {
    refreshEndpoint(200);
    var initialRequests = new CountDownLatch(2);
    server.createContext(
        "/resource",
        exchange -> {
          if ("Bearer old".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
            initialRequests.countDown();
            try {
              if (!initialRequests.await(5, TimeUnit.SECONDS))
                throw new IOException("Missing concurrent request");
            } catch (InterruptedException error) {
              throw new IOException(error);
            }
            respond(exchange, 401, "{}");
          } else respond(exchange, 200, "\"ok\"");
        });
    var workers = Executors.newFixedThreadPool(2);
    try {
      Callable<String> call =
          () ->
              await(
                  api.<String>callApi(
                      "GET", "resource", null, String.class, Map.of(), Runnable::run));
      var results = workers.invokeAll(List.of(call, call), 10, TimeUnit.SECONDS);
      for (var result : results) assertEquals("ok", result.get());
      assertEquals(1, refreshes.get());
    } finally {
      workers.shutdownNow();
    }
  }

  @Test
  public void logoutDuringRefreshIsNotOverwritten() {
    server.createContext("/resource", exchange -> respond(exchange, 401, "{}"));
    server.createContext(
        "/auth/refresh",
        exchange -> {
          store.logout();
          respond(exchange, 200, "{\"accessToken\":\"new\",\"refreshToken\":\"rotated\"}");
        });
    assertThrows(
        ApiException.class,
        () -> await(api.callApi("GET", "resource", null, String.class, Map.of(), Runnable::run)));
    assertNull(store.tokens());
  }
}
