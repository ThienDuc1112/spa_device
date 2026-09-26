package com.company.domain;

import com.company.domain.Models.*;
import java.math.BigDecimal;
import java.util.UUID;

public interface DisposalRepository {
  Disposal find(UUID id, long storeId);

  Disposal lock(UUID id, long storeId);

  java.util.List<Disposal> list(long storeId, int offset);

  java.util.List<DisposalItem> items(UUID id);

  java.util.List<java.util.Map<String, Object>> history(UUID id);

  int create(UUID id, long storeId, String remarks, long actorId);

  int item(UUID id, long productId, BigDecimal quantity, String reason);

  int transition(UUID id, String status, long version);

  int historyAdd(UUID id, String oldStatus, String newStatus, long actorId);
}
