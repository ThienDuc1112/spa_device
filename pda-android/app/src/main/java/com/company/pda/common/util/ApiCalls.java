package com.company.pda.common.util;

import com.company.pda.common.exception.ApiException;
import com.company.pda.data.remote.dto.auth.AuthDto.Refresh;
import com.company.pda.data.remote.dto.auth.AuthDto.Tokens;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/** JSON transport that schedules requests on an IO executor. */
public final class ApiCalls {
  public interface TokenStore {
    Tokens tokens();

    void tokens(Tokens tokens);

    void logout();
  }

  private final String baseUrl;
  private final TokenStore store;
  private final Gson gson = new Gson();
  private final Object refreshLock = new Object();

  public ApiCalls(String baseUrl, TokenStore store) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.store = store;
  }

  public CompletableFuture<Tokens> extendSession(Executor executor) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Tokens current = store.tokens();
            if (current == null)
              throw new ApiException(401, "SESSION_EXPIRED", "Please sign in again");
            Tokens renewed = refresh(current, true);
            if (renewed == null)
              throw new ApiException(401, "SESSION_EXPIRED", "Please sign in again");
            return renewed;
          } catch (IOException error) {
            throw new CompletionException(error);
          }
        },
        executor);
  }

  private void invalidate(Tokens sent) {
    synchronized (store) {
      Tokens latest = store.tokens();
      if (latest != null && latest.refreshToken.equals(sent.refreshToken)) store.logout();
    }
  }

  public static String path(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException impossible) {
      throw new AssertionError(impossible);
    }
  }

  /** Runs HTTP on the supplied executor and returns the decoded response. */
  public <T> CompletableFuture<T> callApi(
      String method,
      String path,
      Object body,
      Type type,
      Map<String, String> headers,
      Executor executor) {
    Map<String, String> requestHeaders = Map.copyOf(headers);
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            boolean authenticated =
                !path.startsWith("auth/") && !requestHeaders.containsKey("X-Device-Secret");
            Tokens sent = authenticated ? accessTokens() : null;
            for (int attempt = 0; ; attempt++) {
              HttpURLConnection http = (HttpURLConnection) new URL(baseUrl + path).openConnection();
              ApiException failure;
              try {
                http.setConnectTimeout(10_000);
                http.setReadTimeout(20_000);
                // Keep credentials on the configured origin and avoid replaying writes on
                // redirects.
                http.setInstanceFollowRedirects(false);
                http.setRequestMethod(method);
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("PDA_TYPE", "ANDROID");
                for (var header : requestHeaders.entrySet())
                  http.setRequestProperty(header.getKey(), header.getValue());
                if (sent != null)
                  http.setRequestProperty("Authorization", "Bearer " + sent.accessToken);

                if (body != null) {
                  byte[] json = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
                  http.setDoOutput(true);
                  http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                  http.setFixedLengthStreamingMode(json.length);
                  try (var output = http.getOutputStream()) {
                    output.write(json);
                  }
                }

                int status = http.getResponseCode();
                String content;
                try (var input =
                    status >= 200 && status < 300 ? http.getInputStream() : http.getErrorStream()) {
                  var buffer = new ByteArrayOutputStream();
                  if (input != null) {
                    byte[] bytes = new byte[4096];
                    int count;
                    while ((count = input.read(bytes)) != -1) buffer.write(bytes, 0, count);
                  }
                  content = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                }
                if (status < 200 || status >= 300) {
                  String message = "Request failed (" + status + ")";
                  String code = null;
                  try {
                    var json = JsonParser.parseString(content).getAsJsonObject();
                    if (json.has("detail")) message = json.get("detail").getAsString();
                    if (json.has("code") && !json.get("code").isJsonNull())
                      code = json.get("code").getAsString();
                  } catch (RuntimeException ignored) {
                  }
                  throw new ApiException(status, code, message);
                }
                if (type == Void.class || content.trim().isEmpty()) return null;
                try {
                  return gson.fromJson(content, type);
                } catch (RuntimeException error) {
                  throw new IOException("Invalid JSON response", error);
                }
              } catch (ApiException error) {
                failure = error;
              } finally {
                http.disconnect();
              }

              // The failed connection is closed before refreshing and retrying once.
              if (failure.status != 401 || sent == null) throw failure;
              if (attempt > 0
                  || (failure.code != null && !"ACCESS_TOKEN_EXPIRED".equals(failure.code))) {
                invalidate(sent);
                throw failure;
              }
              Tokens renewed = refresh(sent, false);
              if (renewed == null) throw failure;
              sent = renewed;
            }
          } catch (IOException error) {
            throw new CompletionException(error);
          }
        },
        executor);
  }

  private Tokens accessTokens() throws IOException {
    Tokens current = store.tokens();
    if (current == null) return null;
    long now = SessionTime.now(current, System.currentTimeMillis());
    long sessionExpiry = SessionTime.sessionExpiry(current);
    if (sessionExpiry > 0 && sessionExpiry <= now) {
      invalidate(current);
      throw new ApiException(401, "SESSION_EXPIRED", "Session expired; sign in again");
    }
    long accessExpiry = SessionTime.accessExpiry(current);
    if (accessExpiry > 0 && accessExpiry <= now) {
      current = refresh(current, false);
      if (current == null)
        throw new ApiException(401, "SESSION_EXPIRED", "Session expired; sign in again");
    }
    return current;
  }

  private Tokens refresh(Tokens sent, boolean extendSession) throws IOException {
    synchronized (refreshLock) {
      Tokens current = store.tokens();
      if (current == null) return null;
      // Reuse a refresh completed by another request, including rotated refresh tokens.
      if (!current.accessToken.equals(sent.accessToken)) return current;
      if (!current.refreshToken.equals(sent.refreshToken)) return null;
      Tokens renewed;
      try {
        renewed =
            this.<Tokens>callApi(
                    "POST",
                    "auth/refresh",
                    new Refresh(current.refreshToken, extendSession),
                    Tokens.class,
                    Map.of(),
                    Runnable::run)
                .join();
      } catch (CompletionException failure) {
        Throwable cause = failure.getCause();
        if (cause instanceof ApiException && ((ApiException) cause).status == 401)
          invalidate(current);
        if (cause instanceof IOException) throw (IOException) cause;
        throw failure;
      }
      if (renewed == null
          || renewed.accessToken == null
          || renewed.accessToken.trim().isEmpty()
          || renewed.refreshToken == null
          || renewed.refreshToken.trim().isEmpty()) throw new IOException("Invalid token response");
      synchronized (store) {
        Tokens latest = store.tokens();
        if (latest == null || !latest.refreshToken.equals(current.refreshToken)) return null;
        store.tokens(renewed);
      }
      return renewed;
    }
  }
}
