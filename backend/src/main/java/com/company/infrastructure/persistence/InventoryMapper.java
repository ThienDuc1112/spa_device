package com.company.infrastructure.persistence;

import com.company.domain.InventoryRepository;
import com.company.domain.Models.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.ibatis.annotations.*;

@Mapper
public interface InventoryMapper extends InventoryRepository {
  @Select("SELECT pg_advisory_xact_lock(#{storeId})")
  String lockStore(long storeId);

  @Select(
      "SELECT id,product_id,store_id,quantity,version FROM inventories WHERE store_id=#{storeId}"
          + " AND product_id=#{productId}")
  Inventory find(long storeId, long productId);

  @Update(
      "UPDATE inventories SET quantity=#{quantity},version=version+1,updated_at=now() WHERE"
          + " store_id=#{storeId} AND product_id=#{productId} AND version=#{version}")
  int update(long storeId, long productId, BigDecimal quantity, long version);

  @Insert(
      "INSERT INTO"
          + " inventory_adjustments(id,store_id,product_id,old_qty,new_qty,expected_version,reason,created_by)"
          + " VALUES(#{id},#{storeId},#{productId},#{oldQty},#{newQty},#{version},#{reason},#{actorId})")
  int adjustment(
      UUID id,
      long storeId,
      long productId,
      BigDecimal oldQty,
      BigDecimal newQty,
      long version,
      String reason,
      long actorId);

  @Select("SELECT * FROM inventory_adjustments WHERE id=#{id} AND store_id=#{storeId}")
  java.util.Map<String, Object> adjustmentById(UUID id, long storeId);

  @Select(
      value =
          "INSERT INTO"
              + " inventory_transactions(store_id,product_id,transaction_type,qty_before,qty_change,qty_after,adjustment_id,disposal_id,created_by)"
              + " VALUES(#{storeId},#{productId},#{type},#{before},#{change},#{after},#{adjustmentId},#{disposalId},#{actorId})"
              + " RETURNING id",
      affectData = true)
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

  @Select(
      "SELECT * FROM inventory_transactions WHERE store_id=#{storeId} AND id>#{afterId} ORDER BY id"
          + " LIMIT 200")
  java.util.List<java.util.Map<String, Object>> transactions(long storeId, long afterId);
}
