package com.company.domain;

import com.company.domain.Models.*;
import java.math.BigDecimal;
import java.util.UUID;

public interface InventoryRepository {
  String lockStore(long storeId);

  Inventory find(long storeId, long productId);

  int update(long storeId, long productId, BigDecimal quantity, long version);

  int adjustment(
      UUID id,
      long storeId,
      long productId,
      BigDecimal oldQty,
      BigDecimal newQty,
      long version,
      String reason,
      long actorId);

  java.util.Map<String, Object> adjustmentById(UUID id, long storeId);

  Long transaction(
      long storeId,
      long productId,
      String type,
      BigDecimal before,
      BigDecimal change,
      BigDecimal after,
      UUID adjustmentId,
      UUID disposalId,
      long actorId);

  java.util.List<java.util.Map<String, Object>> transactions(long storeId, long afterId);
}
