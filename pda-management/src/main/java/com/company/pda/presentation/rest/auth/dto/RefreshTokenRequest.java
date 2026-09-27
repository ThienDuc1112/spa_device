package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.RefreshTokenCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record RefreshTokenRequest(@NotBlank @Size(max = 4096) String refreshToken) {
  public RefreshTokenCommand toCommand() {
    return new RefreshTokenCommand(refreshToken);
  }
}
