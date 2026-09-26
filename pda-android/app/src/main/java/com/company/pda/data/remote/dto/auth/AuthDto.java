package com.company.pda.data.remote.dto.auth;

public final class AuthDto {
  public record Login(String username, String password) {}

  public record Refresh(String refreshToken) {}

  public static class Tokens {
    public String accessToken, refreshToken;
    public long expiresIn;
  }
}
