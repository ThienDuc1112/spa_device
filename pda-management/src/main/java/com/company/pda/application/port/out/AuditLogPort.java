package com.company.pda.application.port.out;

import com.company.pda.domain.shared.model.AuditInfo;

public interface AuditLogPort {
  int systemAudit(long storeId, String operation, String reference);

  int audit(long actorId, long storeId, String operation, String reference);

  default int append(AuditInfo event) {
    return event.actorId() == null
        ? systemAudit(event.storeId(), event.operation(), event.reference())
        : audit(event.actorId(), event.storeId(), event.operation(), event.reference());
  }
}
