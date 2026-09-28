package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.infrastructure.persistence.mybatis.entity.PdaFindRequestEntity;
import java.time.Instant;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface PdaFindMapper {
  String lastPushEvent(@Param("deviceId") long deviceId, @Param("storeId") long storeId);

  java.util.List<PdaFindRequestEntity> commands(
      @Param("deviceId") long deviceId, @Param("storeId") long storeId);

  PdaFindRequestEntity find(@Param("id") UUID id, @Param("storeId") long storeId);

  int create(
      @Param("id") UUID id,
      @Param("actorId") long actorId,
      @Param("storeId") long storeId,
      @Param("deviceId") long deviceId,
      @Param("expiresAt") Instant expiresAt);

  int status(@Param("id") UUID id, @Param("status") String status);

  int sent(UUID id);

  int log(
      @Param("id") UUID id,
      @Param("deviceId") long deviceId,
      @Param("event") String event,
      @Param("message") String message);

  java.util.List<PdaFindRequestEntity> expired();
}
