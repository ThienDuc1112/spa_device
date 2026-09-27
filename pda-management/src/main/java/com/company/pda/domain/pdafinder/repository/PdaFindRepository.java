package com.company.pda.domain.pdafinder.repository;

import com.company.pda.domain.pdafinder.model.PdaFindRequest;
import java.time.Instant;
import java.util.UUID;

public interface PdaFindRepository {
  PdaFindRequest find(UUID id, long storeId);

  int create(UUID id, long actorId, long storeId, long deviceId, Instant expiresAt);

  int status(UUID id, String status);

  int sent(UUID id);

  int log(UUID id, long deviceId, String event, String message);

  java.util.List<PdaFindRequest> expired();
}
