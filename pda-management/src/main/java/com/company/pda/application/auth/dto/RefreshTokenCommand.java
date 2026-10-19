package com.company.pda.application.auth.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RefreshTokenCommand {
  private final String refreshToken;
  private final boolean extendSession;

  @java.beans.ConstructorProperties({"refreshToken"})
  public RefreshTokenCommand(String refreshToken) {
    this(refreshToken, false);
  }

  public RefreshTokenCommand(String refreshToken, boolean extendSession) {
    this.refreshToken = refreshToken;
    this.extendSession = extendSession;
  }

  public boolean extendSession() {
    return extendSession;
  }

  public String refreshToken() {
    return refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }
}
