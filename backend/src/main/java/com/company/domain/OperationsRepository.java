package com.company.domain;

import com.company.domain.Models.*;

public interface OperationsRepository {
  int systemAudit(long storeId, String operation, String reference);

  int audit(long actorId, long storeId, String operation, String reference);

  int enqueue(String type, String aggregateId, String payload);

  Outbox next();

  int done(long id);

  int retry(long id);
}
