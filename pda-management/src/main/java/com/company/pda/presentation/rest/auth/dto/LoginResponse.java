package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.TokenResult;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class LoginResponse {
  private final String accessToken;
  private final String refreshToken;
  private final long expiresIn;
  @com.fasterxml.jackson.annotation.JsonInclude(
      com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT)
  public long serverTime, accessExpiresAt, sessionExpiresAt, warningSeconds;

  @java.beans.ConstructorProperties({"accessToken", "refreshToken", "expiresIn"})
  public LoginResponse(String accessToken, String refreshToken, long expiresIn) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresIn = expiresIn;
  }

  public String accessToken() {
    return accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String refreshToken() {
    return refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public long expiresIn() {
    return expiresIn;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public static LoginResponse from(TokenResult result) {
    return from(result, false);
  }

  public static LoginResponse from(TokenResult result, boolean sessionMetadata) {
    LoginResponse response =
        new LoginResponse(result.accessToken(), result.refreshToken(), result.expiresIn());
    if (!sessionMetadata) return response;
    response.serverTime = result.serverTime;
    response.accessExpiresAt = result.accessExpiresAt;
    response.sessionExpiresAt = result.sessionExpiresAt;
    response.warningSeconds = result.warningSeconds;
    return response;
  }
}
