package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.TokenResult;

public record LoginResponse(String accessToken, String refreshToken, long expiresIn) {
  public static LoginResponse from(TokenResult result) {
    return new LoginResponse(result.accessToken(), result.refreshToken(), result.expiresIn());
  }
}
