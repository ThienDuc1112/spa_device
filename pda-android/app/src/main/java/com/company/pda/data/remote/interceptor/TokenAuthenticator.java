package com.company.pda.data.remote.interceptor;

import com.company.pda.data.local.preferences.TokenStorage;
import com.company.pda.data.remote.api.AuthApi;
import com.company.pda.data.remote.dto.auth.AuthDto.Refresh;
import java.io.IOException;
import okhttp3.*;

public final class TokenAuthenticator implements Authenticator {
  private final Object refreshLock = new Object();
  private final TokenStorage store;
  private final AuthApi api;

  public TokenAuthenticator(TokenStorage store, AuthApi api) {
    this.store = store;
    this.api = api;
  }

  public Request authenticate(Route route, Response response) throws IOException {
    if (response.request().url().encodedPath().startsWith("/auth/")
        || response.request().header("X-Device-Secret") != null
        || response.priorResponse() != null) return null;
    synchronized (refreshLock) {
      var tokens = store.tokens();
      if (tokens == null) return null;
      if (("Bearer " + tokens.accessToken).equals(response.request().header("Authorization"))) {
        var result = api.refresh(new Refresh(tokens.refreshToken)).execute();
        if (!result.isSuccessful() || result.body() == null) {
          if (result.code() == 401)
            synchronized (store) {
              var current = store.tokens();
              if (current != null && current.refreshToken.equals(tokens.refreshToken))
                store.logout();
            }
          if (result.errorBody() != null) result.errorBody().close();
          return null;
        }
        synchronized (store) {
          var current = store.tokens();
          if (current == null || !current.refreshToken.equals(tokens.refreshToken)) return null;
          tokens = result.body();
          store.tokens(tokens);
        }
      }
      return response
          .request()
          .newBuilder()
          .header("Authorization", "Bearer " + tokens.accessToken)
          .build();
    }
  }
}
