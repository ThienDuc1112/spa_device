package com.company.pda.application.port.out;

import com.company.pda.domain.shared.model.Outbox;

public interface OperationsRepository extends AuditLogPort {
  int audit(long actorId, long storeId, String operation, String reference);

  int enqueue(String type, String aggregateId, String payload);

  Outbox next();

  int done(long id);

  int retry(long id);
}
