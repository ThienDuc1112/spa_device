package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.RefreshTokenCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RefreshTokenRequest {
  private final @NotBlank @Size(max = 4096) String refreshToken;

  @java.beans.ConstructorProperties({"refreshToken"})
  public RefreshTokenRequest(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String refreshToken() {
    return refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public RefreshTokenCommand toCommand() {
    return new RefreshTokenCommand(refreshToken);
  }
}
