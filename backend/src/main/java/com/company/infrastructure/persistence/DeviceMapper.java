package com.company.infrastructure.persistence;

import com.company.domain.DeviceRepository;
import com.company.domain.Models.*;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DeviceMapper extends DeviceRepository {
  @Select(
      "SELECT id,device_code,device_name,store_id,credential_hash,fcm_token,last_active_at FROM"
          + " devices WHERE id=#{id} AND store_id=#{storeId}")
  Device find(long id, long storeId);

  @Select(
      "SELECT id,device_code,device_name,store_id,credential_hash,fcm_token,last_active_at FROM"
          + " devices WHERE id=#{id}")
  Device identity(long id);

  @Select(
      "SELECT id,device_code,device_name,store_id,credential_hash,fcm_token,last_active_at FROM"
          + " devices WHERE store_id=#{storeId} ORDER BY id LIMIT 500")
  java.util.List<Device> list(long storeId);

  @Select(
      value =
          "INSERT INTO"
              + " devices(device_code,device_name,store_id,registered_by,credential_hash,fcm_token,last_active_at)"
              + " VALUES(#{code},#{name},#{storeId},#{actorId},#{hash},#{token},now()) RETURNING"
              + " id",
      affectData = true)
  Long register(String code, String name, long storeId, long actorId, String hash, String token);

  @Update(
      "UPDATE devices SET fcm_token=#{token},last_active_at=now(),updated_at=now() WHERE id=#{id}")
  int token(long id, String token);

  @Update(
      "UPDATE devices SET fcm_token=NULL,updated_at=now() WHERE id=#{id} AND fcm_token=#{token}")
  int invalidate(long id, String token);
}
