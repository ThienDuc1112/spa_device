package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.auth.AuthDto.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class AuthApi {
  private final ApiCalls api;
  private final Executor executor;

  public AuthApi(ApiCalls api, Executor executor) {
    this.api = api;
    this.executor = executor;
  }

  public CompletableFuture<Tokens> login(Login body) {
    return api.callApi("POST", "auth/login", body, Tokens.class, Map.of(), executor);
  }

  public CompletableFuture<Tokens> refresh(Refresh body) {
    return api.callApi("POST", "auth/refresh", body, Tokens.class, Map.of(), executor);
  }
}
