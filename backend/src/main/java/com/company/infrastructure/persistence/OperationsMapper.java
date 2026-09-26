package com.company.infrastructure.persistence;

import com.company.domain.Models.*;
import com.company.domain.OperationsRepository;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OperationsMapper extends OperationsRepository {
  @Insert(
      "INSERT INTO audit_logs(store_id,operation,reference)"
          + " VALUES(#{storeId},#{operation},#{reference})")
  int systemAudit(long storeId, String operation, String reference);

  @Insert(
      "INSERT INTO audit_logs(actor_id,store_id,operation,reference)"
          + " VALUES(#{actorId},#{storeId},#{operation},#{reference})")
  int audit(long actorId, long storeId, String operation, String reference);

  @Insert(
      "INSERT INTO outbox_events(event_type,aggregate_id,payload)"
          + " VALUES(#{type},#{aggregateId},CAST(#{payload} AS jsonb))")
  int enqueue(String type, String aggregateId, String payload);

  @Select(
      "SELECT id,event_type,aggregate_id,payload::text,attempts FROM outbox_events WHERE"
          + " processed_at IS NULL AND available_at<=now() ORDER BY id LIMIT 1 FOR UPDATE SKIP"
          + " LOCKED")
  Outbox next();

  @Update("UPDATE outbox_events SET processed_at=now() WHERE id=#{id}")
  int done(long id);

  @Update(
      "UPDATE outbox_events SET"
          + " attempts=attempts+1,available_at=now()+LEAST(300,power(2,LEAST(attempts+1,8))) *"
          + " interval '1 second' WHERE id=#{id}")
  int retry(long id);
}
