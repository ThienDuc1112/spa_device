package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.infrastructure.persistence.mybatis.entity.OutboxEntity;
import org.apache.ibatis.annotations.Param;

public interface OperationsMapper {
  int systemAudit(
      @Param("storeId") long storeId,
      @Param("operation") String operation,
      @Param("reference") String reference);

  int audit(
      @Param("actorId") long actorId,
      @Param("storeId") long storeId,
      @Param("operation") String operation,
      @Param("reference") String reference);

  int enqueue(
      @Param("type") String type,
      @Param("aggregateId") String aggregateId,
      @Param("payload") String payload);

  OutboxEntity next();

  int done(long id);

  int retry(long id);
}
