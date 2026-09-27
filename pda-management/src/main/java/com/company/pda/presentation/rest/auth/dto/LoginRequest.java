package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.LoginCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record LoginRequest(
    @NotBlank @Size(max = 100) String username, @NotBlank @Size(max = 200) String password) {
  public LoginCommand toCommand() {
    return new LoginCommand(username, password);
  }
}
