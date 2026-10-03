package com.company.pda.infrastructure.persistence.mybatis.repository;

import com.company.pda.domain.inventory.model.Inventory;
import com.company.pda.domain.inventory.model.InventoryAdjustment;
import com.company.pda.domain.inventory.repository.InventoryRepository;
import com.company.pda.infrastructure.persistence.mybatis.converter.InventoryEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.mapper.InventoryMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisInventoryRepository implements InventoryRepository {
  private final InventoryMapper mapper;

  public MyBatisInventoryRepository(InventoryMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public String lockStore(long storeId) {
    return mapper.lockStore(storeId);
  }

  @Override
  public Inventory find(long storeId, long productId) {
    return InventoryEntityMapper.toDomain(mapper.find(storeId, productId));
  }

  @Override
  public int update(long storeId, long productId, BigDecimal quantity, long version) {
    return mapper.update(storeId, productId, quantity, version);
  }

  @Override
  public int adjustment(
      UUID id,
      long storeId,
      long productId,
      BigDecimal oldQty,
      BigDecimal newQty,
      long version,
      String reason,
      long actorId) {
    return mapper.adjustment(id, storeId, productId, oldQty, newQty, version, reason, actorId);
  }

  @Override
  public InventoryAdjustment adjustmentById(UUID id, long storeId) {
    lombok.val row = mapper.adjustmentById(id, storeId);
    return row == null
        ? null
        : new InventoryAdjustment(
            (java.util.UUID) row.get("id"),
            ((Number) row.get("product_id")).longValue(),
            (java.math.BigDecimal) row.get("new_qty"),
            ((Number) row.get("expected_version")).longValue(),
            (String) row.get("reason"),
            ((Number) row.get("created_by")).longValue());
  }

  @Override
  public Long transaction(
      long storeId,
      long productId,
      String type,
      BigDecimal before,
      BigDecimal change,
      BigDecimal after,
      UUID adjustmentId,
      UUID disposalId,
      long actorId) {
    return mapper.transaction(
        storeId, productId, type, before, change, after, adjustmentId, disposalId, actorId);
  }

  @Override
  public java.util.List<java.util.Map<String, Object>> transactions(long storeId, long afterId) {
    return mapper.transactions(storeId, afterId);
  }
}
