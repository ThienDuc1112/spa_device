package com.company.infrastructure.persistence;

import com.company.domain.DisposalRepository;
import com.company.domain.Models.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DisposalMapper extends DisposalRepository {
  @Select(
      "SELECT id,store_id,status,remarks,created_by,version,created_at FROM disposals WHERE"
          + " id=#{id} AND store_id=#{storeId}")
  Disposal find(UUID id, long storeId);

  @Select(
      "SELECT id,store_id,status,remarks,created_by,version,created_at FROM disposals WHERE"
          + " id=#{id} AND store_id=#{storeId} FOR UPDATE")
  Disposal lock(UUID id, long storeId);

  @Select(
      "SELECT id,store_id,status,remarks,created_by,version,created_at FROM disposals WHERE"
          + " store_id=#{storeId} ORDER BY created_at DESC,id LIMIT 50 OFFSET #{offset}")
  java.util.List<Disposal> list(long storeId, int offset);

  @Select(
      "SELECT product_id,quantity,reason FROM disposal_items WHERE disposal_id=#{id} ORDER BY"
          + " product_id")
  java.util.List<DisposalItem> items(UUID id);

  @Select(
      "SELECT old_status,new_status,changed_by,changed_at FROM disposal_histories WHERE"
          + " disposal_id=#{id} ORDER BY id")
  java.util.List<java.util.Map<String, Object>> history(UUID id);

  @Insert(
      "INSERT INTO disposals(id,store_id,remarks,created_by)"
          + " VALUES(#{id},#{storeId},#{remarks},#{actorId})")
  int create(UUID id, long storeId, String remarks, long actorId);

  @Insert(
      "INSERT INTO disposal_items(disposal_id,product_id,quantity,reason)"
          + " VALUES(#{id},#{productId},#{quantity},#{reason})")
  int item(UUID id, long productId, BigDecimal quantity, String reason);

  @Update(
      "UPDATE disposals SET status=#{status},version=version+1,updated_at=now() WHERE id=#{id} AND"
          + " version=#{version} AND status='PENDING'")
  int transition(UUID id, String status, long version);

  @Insert(
      "INSERT INTO disposal_histories(disposal_id,old_status,new_status,changed_by)"
          + " VALUES(#{id},#{oldStatus},#{newStatus},#{actorId})")
  int historyAdd(UUID id, String oldStatus, String newStatus, long actorId);
}
