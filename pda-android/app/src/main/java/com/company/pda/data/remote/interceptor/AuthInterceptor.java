package com.company.pda.data.remote.interceptor;

import com.company.pda.data.local.preferences.TokenStorage;
import java.io.IOException;
import okhttp3.*;

public final class AuthInterceptor implements Interceptor {
  private final TokenStorage store;

  public AuthInterceptor(TokenStorage store) {
    this.store = store;
  }

  public Response intercept(Chain chain) throws IOException {
    var request = chain.request();
    var tokens = store.tokens();
    if (tokens != null
        && !request.url().encodedPath().startsWith("/auth/")
        && request.header("X-Device-Secret") == null)
      request =
          request.newBuilder().header("Authorization", "Bearer " + tokens.accessToken).build();
    return chain.proceed(request);
  }
}
