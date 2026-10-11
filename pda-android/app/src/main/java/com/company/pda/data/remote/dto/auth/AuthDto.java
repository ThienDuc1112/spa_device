package com.company.pda.data.remote.dto.auth;

public final class AuthDto {
  public static final class Login {
    private final String username;
    private final String password;

    public Login(String username, String password) {
      this.username = username;
      this.password = password;
    }

    public String username() {
      return username;
    }

    public String password() {
      return password;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Login other)) return false;
      return java.util.Objects.equals(username, other.username)
          && java.util.Objects.equals(password, other.password);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(username, password);
    }

    @Override
    public String toString() {
      return "Login[username=" + username + ", password=" + password + "]";
    }
  }

  public static final class Refresh {
    private final String refreshToken;

    public Refresh(String refreshToken) {
      this.refreshToken = refreshToken;
    }

    public String refreshToken() {
      return refreshToken;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Refresh other)) return false;
      return java.util.Objects.equals(refreshToken, other.refreshToken);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(refreshToken);
    }

    @Override
    public String toString() {
      return "Refresh[refreshToken=" + refreshToken + "]";
    }
  }

  public static class Tokens {
    public String accessToken, refreshToken;
    public long expiresIn;
  }
}
