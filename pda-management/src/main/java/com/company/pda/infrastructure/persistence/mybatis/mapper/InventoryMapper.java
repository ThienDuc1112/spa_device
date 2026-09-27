package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.infrastructure.persistence.mybatis.entity.InventoryEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface InventoryMapper {
  String lockStore(long storeId);

  InventoryEntity find(@Param("storeId") long storeId, @Param("productId") long productId);

  int update(
      @Param("storeId") long storeId,
      @Param("productId") long productId,
      @Param("quantity") BigDecimal quantity,
      @Param("version") long version);

  int adjustment(
      @Param("id") UUID id,
      @Param("storeId") long storeId,
      @Param("productId") long productId,
      @Param("oldQty") BigDecimal oldQty,
      @Param("newQty") BigDecimal newQty,
      @Param("version") long version,
      @Param("reason") String reason,
      @Param("actorId") long actorId);

  java.util.Map<String, Object> adjustmentById(
      @Param("id") UUID id, @Param("storeId") long storeId);

  Long transaction(
      @Param("storeId") long storeId,
      @Param("productId") long productId,
      @Param("type") String type,
      @Param("before") BigDecimal before,
      @Param("change") BigDecimal change,
      @Param("after") BigDecimal after,
      @Param("adjustmentId") UUID adjustmentId,
      @Param("disposalId") UUID disposalId,
      @Param("actorId") long actorId);

  java.util.List<java.util.Map<String, Object>> transactions(
      @Param("storeId") long storeId, @Param("afterId") long afterId);
}
