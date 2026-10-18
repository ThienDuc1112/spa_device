package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.auth.AuthDto.*;

public final class AuthApi {
  private final ApiCalls api;

  public AuthApi(ApiCalls api) {
    this.api = api;
  }

  public ApiCalls.Request<Tokens> login(Login body) {
    return api.request("POST", "auth/login", body, Tokens.class);
  }

  public ApiCalls.Request<Tokens> refresh(Refresh body) {
    return api.request("POST", "auth/refresh", body, Tokens.class);
  }
}
