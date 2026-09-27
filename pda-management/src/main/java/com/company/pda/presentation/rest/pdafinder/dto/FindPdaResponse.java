package com.company.pda.presentation.rest.pdafinder.dto;

import com.company.pda.application.pdafinder.dto.FindPdaResult;
import java.time.Instant;
import java.util.UUID;

public record FindPdaResponse(
    UUID id, long requesterId, long storeId, long deviceId, String status, Instant expiresAt) {
  public static FindPdaResponse from(FindPdaResult r) {
    return new FindPdaResponse(
        r.id(), r.requesterId(), r.storeId(), r.deviceId(), r.status(), r.expiresAt());
  }
}
