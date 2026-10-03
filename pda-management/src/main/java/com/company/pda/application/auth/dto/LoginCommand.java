package com.company.pda.application.auth.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class LoginCommand {
  private final String username;
  private final String password;

  @java.beans.ConstructorProperties({"username", "password"})
  public LoginCommand(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String username() {
    return username;
  }

  public String getUsername() {
    return username;
  }

  public String password() {
    return password;
  }

  public String getPassword() {
    return password;
  }
}
