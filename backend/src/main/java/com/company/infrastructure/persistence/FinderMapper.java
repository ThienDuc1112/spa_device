package com.company.infrastructure.persistence;

import com.company.domain.FinderRepository;
import com.company.domain.Models.*;
import java.time.Instant;
import java.util.UUID;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FinderMapper extends FinderRepository {
  @Select(
      "SELECT id,requester_id,store_id,device_id,status,expires_at FROM pda_find_requests WHERE"
          + " id=#{id} AND store_id=#{storeId}")
  FindRequest find(UUID id, long storeId);

  @Insert(
      "INSERT INTO pda_find_requests(id,requester_id,store_id,device_id,status,expires_at)"
          + " VALUES(#{id},#{actorId},#{storeId},#{deviceId},'QUEUED',#{expiresAt})")
  int create(UUID id, long actorId, long storeId, long deviceId, Instant expiresAt);

  @Update(
      "UPDATE pda_find_requests SET status=#{status},updated_at=now() WHERE id=#{id} AND status IN"
          + " ('QUEUED','SENT','RINGING')")
  int status(UUID id, String status);

  @Update(
      "UPDATE pda_find_requests SET status='SENT',updated_at=now() WHERE id=#{id} AND"
          + " status='QUEUED'")
  int sent(UUID id);

  @Insert(
      "INSERT INTO pda_alert_logs(request_id,device_id,event,message)"
          + " VALUES(#{id},#{deviceId},#{event},#{message})")
  int log(UUID id, long deviceId, String event, String message);

  @Select(
      "SELECT id,requester_id,store_id,device_id,status,expires_at FROM pda_find_requests WHERE"
          + " expires_at<=now() AND status IN ('QUEUED','SENT','RINGING') FOR UPDATE SKIP LOCKED")
  java.util.List<FindRequest> expired();
}
