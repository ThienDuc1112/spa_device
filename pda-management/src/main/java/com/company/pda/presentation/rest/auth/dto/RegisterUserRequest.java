package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.RegisterUserCommand;
import jakarta.validation.constraints.*;

public record RegisterUserRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]{2,99}") String username,
    @NotBlank @Size(min = 12, max = 72) String password,
    @NotBlank @Size(max = 255) String fullName,
    @Email @Size(max = 255) String email) {
  public RegisterUserCommand toCommand() {
    return new RegisterUserCommand(username, password, fullName, email);
  }

  @Override
  public String toString() {
    return "RegisterUserRequest[username=" + username + "]";
  }
}
