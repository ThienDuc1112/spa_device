package com.company.pda.application.auth.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class TokenResult {
  private final String accessToken;
  private final String refreshToken;
  private final long expiresIn;

  @java.beans.ConstructorProperties({"accessToken", "refreshToken", "expiresIn"})
  public TokenResult(String accessToken, String refreshToken, long expiresIn) {
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
}
