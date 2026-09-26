package com.company.domain;

import com.company.domain.Models.*;
import java.time.Instant;
import java.util.UUID;

public interface FinderRepository {
  FindRequest find(UUID id, long storeId);

  int create(UUID id, long actorId, long storeId, long deviceId, Instant expiresAt);

  int status(UUID id, String status);

  int sent(UUID id);

  int log(UUID id, long deviceId, String event, String message);

  java.util.List<FindRequest> expired();
}
