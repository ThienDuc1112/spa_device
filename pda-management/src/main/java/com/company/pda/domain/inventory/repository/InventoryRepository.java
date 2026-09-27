package com.company.pda.domain.inventory.repository;

import com.company.pda.domain.inventory.model.Inventory;
import com.company.pda.domain.inventory.model.InventoryAdjustment;
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

  InventoryAdjustment adjustmentById(UUID id, long storeId);

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
