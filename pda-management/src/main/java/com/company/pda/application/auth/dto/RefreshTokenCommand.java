package com.company.pda.application.auth.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RefreshTokenCommand {
  private final String refreshToken;

  @java.beans.ConstructorProperties({"refreshToken"})
  public RefreshTokenCommand(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String refreshToken() {
    return refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }
}
