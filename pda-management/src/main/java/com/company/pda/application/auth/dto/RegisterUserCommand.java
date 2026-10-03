package com.company.pda.application.auth.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RegisterUserCommand {
  private final String username;
  private final String password;
  private final String fullName;
  private final String email;

  @java.beans.ConstructorProperties({"username", "password", "fullName", "email"})
  public RegisterUserCommand(String username, String password, String fullName, String email) {
    this.username = username;
    this.password = password;
    this.fullName = fullName;
    this.email = email;
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

  public String fullName() {
    return fullName;
  }

  public String getFullName() {
    return fullName;
  }

  public String email() {
    return email;
  }

  public String getEmail() {
    return email;
  }
}
