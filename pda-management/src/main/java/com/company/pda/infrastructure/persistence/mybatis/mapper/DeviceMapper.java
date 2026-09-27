package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.infrastructure.persistence.mybatis.entity.DeviceEntity;
import org.apache.ibatis.annotations.Param;

public interface DeviceMapper {
  DeviceEntity find(@Param("id") long id, @Param("storeId") long storeId);

  DeviceEntity identity(long id);

  java.util.List<DeviceEntity> list(long storeId);

  Long register(
      @Param("code") String code,
      @Param("name") String name,
      @Param("storeId") long storeId,
      @Param("actorId") long actorId,
      @Param("hash") String hash,
      @Param("token") String token);

  int token(@Param("id") long id, @Param("token") String token);

  int invalidate(@Param("id") long id, @Param("token") String token);
}
