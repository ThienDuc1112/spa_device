package com.company.pda.application.pdafinder.dto;

import com.company.pda.domain.pdafinder.model.PdaFindRequest;
import java.time.Instant;
import java.util.UUID;

public record FindPdaResult(
    UUID id, long requesterId, long storeId, long deviceId, String status, Instant expiresAt) {
  public static FindPdaResult from(PdaFindRequest r) {
    return new FindPdaResult(
        r.id(), r.requesterId(), r.storeId(), r.deviceId(), r.status(), r.expiresAt());
  }
}
