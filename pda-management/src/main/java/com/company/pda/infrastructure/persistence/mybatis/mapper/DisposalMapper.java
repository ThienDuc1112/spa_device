package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.infrastructure.persistence.mybatis.entity.DisposalEntity;
import com.company.pda.infrastructure.persistence.mybatis.entity.DisposalItemEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface DisposalMapper {
  DisposalEntity find(@Param("id") UUID id, @Param("storeId") long storeId);

  DisposalEntity lock(@Param("id") UUID id, @Param("storeId") long storeId);

  java.util.List<DisposalEntity> list(@Param("storeId") long storeId, @Param("offset") int offset);

  java.util.List<DisposalItemEntity> items(UUID id);

  java.util.List<java.util.Map<String, Object>> history(UUID id);

  int create(
      @Param("id") UUID id,
      @Param("storeId") long storeId,
      @Param("remarks") String remarks,
      @Param("actorId") long actorId);

  int item(
      @Param("id") UUID id,
      @Param("productId") long productId,
      @Param("quantity") BigDecimal quantity,
      @Param("reason") String reason);

  int transition(
      @Param("id") UUID id, @Param("status") String status, @Param("version") long version);

  int historyAdd(
      @Param("id") UUID id,
      @Param("oldStatus") String oldStatus,
      @Param("newStatus") String newStatus,
      @Param("actorId") long actorId);
}
