package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.LoginCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class LoginRequest {
  private final @NotBlank @Size(max = 100) String username;
  private final @NotBlank @Size(max = 200) String password;

  @java.beans.ConstructorProperties({"username", "password"})
  public LoginRequest(String username, String password) {
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

  public LoginCommand toCommand() {
    return new LoginCommand(username, password);
  }
}
