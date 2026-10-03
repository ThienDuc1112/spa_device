package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.RegisterUserCommand;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RegisterUserRequest {
  private final @NotBlank @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]{2,99}") String username;
  private final @NotBlank @Size(min = 12, max = 72) String password;
  private final @NotBlank @Size(max = 255) String fullName;
  private final @Email @Size(max = 255) String email;

  @java.beans.ConstructorProperties({"username", "password", "fullName", "email"})
  public RegisterUserRequest(String username, String password, String fullName, String email) {
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

  public RegisterUserCommand toCommand() {
    return new RegisterUserCommand(username, password, fullName, email);
  }

  @Override
  public String toString() {
    return "RegisterUserRequest[username=" + username + "]";
  }
}
