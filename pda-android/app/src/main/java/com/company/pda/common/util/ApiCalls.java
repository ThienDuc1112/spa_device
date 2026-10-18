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

/** Synchronous JSON transport; execute on an IO executor. */
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

  public static String path(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException impossible) {
      throw new AssertionError(impossible);
    }
  }

  public <T> Request<T> request(String method, String path, Object body, Type type) {
    return request(method, path, body, type, Map.of());
  }

  public <T> Request<T> request(
      String method, String path, Object body, Type type, Map<String, String> headers) {
    return new Request<>(method, path, body, type, Map.copyOf(headers));
  }

  public static <T> T execute(ApiCalls.Request<T> request) throws IOException {
    return request.execute();
  }

  public final class Request<T> {
    private final String method, path;
    private final Object body;
    private final Type type;
    private final Map<String, String> headers;
    private volatile boolean cancelled;
    private HttpURLConnection connection;

    private Request(
        String method, String path, Object body, Type type, Map<String, String> headers) {
      this.method = method;
      this.path = path;
      this.body = body;
      this.type = type;
      this.headers = headers;
    }

    public URL url() throws IOException {
      return new URL(baseUrl + path);
    }

    public synchronized void cancel() {
      cancelled = true;
      if (connection != null) connection.disconnect();
    }

    public T execute() throws IOException {
      boolean authenticated = !path.startsWith("auth/") && !headers.containsKey("X-Device-Secret");
      Tokens sent = authenticated ? store.tokens() : null;
      try {
        return send(sent);
      } catch (ApiException error) {
        if (error.status != 401 || !authenticated || sent == null || cancelled) throw error;
        Tokens renewed = refresh(sent);
        if (renewed == null) throw error;
        return send(renewed);
      }
    }

    private T send(Tokens tokens) throws IOException {
      HttpURLConnection http = (HttpURLConnection) url().openConnection();
      synchronized (this) {
        if (cancelled) {
          http.disconnect();
          throw new IOException("Request cancelled");
        }
        connection = http;
      }
      try {
        http.setConnectTimeout(10_000);
        http.setReadTimeout(20_000);
        // Keep credentials on the configured origin and avoid replaying writes on redirects.
        http.setInstanceFollowRedirects(false);
        http.setRequestMethod(method);
        http.setRequestProperty("Accept", "application/json");
        for (var header : headers.entrySet())
          http.setRequestProperty(header.getKey(), header.getValue());
        if (tokens != null)
          http.setRequestProperty("Authorization", "Bearer " + tokens.accessToken);
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
        if (cancelled) throw new IOException("Request cancelled");
        if (status < 200 || status >= 300) {
          String message = "Request failed (" + status + ")";
          try {
            var json = JsonParser.parseString(content).getAsJsonObject();
            if (json.has("detail")) message = json.get("detail").getAsString();
          } catch (RuntimeException ignored) {
          }
          throw new ApiException(status, message);
        }
        if (type == Void.class || content.trim().isEmpty()) return null;
        try {
          return gson.fromJson(content, type);
        } catch (RuntimeException error) {
          throw new IOException("Invalid JSON response", error);
        }
      } finally {
        synchronized (this) {
          connection = null;
        }
        http.disconnect();
      }
    }
  }

  private Tokens refresh(Tokens sent) throws IOException {
    synchronized (refreshLock) {
      Tokens current = store.tokens();
      if (current == null) return null;
      // Reuse a refresh completed by another request, including rotated refresh tokens.
      if (!current.accessToken.equals(sent.accessToken)) return current;
      if (!current.refreshToken.equals(sent.refreshToken)) return null;
      Tokens renewed;
      try {
        renewed =
            this.<Tokens>request(
                    "POST", "auth/refresh", new Refresh(current.refreshToken), Tokens.class)
                .execute();
      } catch (ApiException error) {
        if (error.status == 401) {
          synchronized (store) {
            Tokens latest = store.tokens();
            if (latest != null && latest.refreshToken.equals(current.refreshToken)) store.logout();
          }
          return null;
        }
        throw error;
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
