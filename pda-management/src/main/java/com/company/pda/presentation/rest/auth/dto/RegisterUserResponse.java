package com.company.pda.presentation.rest.auth.dto;

import com.company.pda.application.auth.dto.RegisterUserResult;

public record RegisterUserResponse(long id, String username, long storeId, String role) {
  public static RegisterUserResponse from(RegisterUserResult result) {
    return new RegisterUserResponse(
        result.id(), result.username(), result.storeId(), result.role());
  }
}
