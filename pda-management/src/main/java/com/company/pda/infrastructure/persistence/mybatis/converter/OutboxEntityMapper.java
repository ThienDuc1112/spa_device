package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.shared.model.Outbox;
import com.company.pda.infrastructure.persistence.mybatis.entity.OutboxEntity;

public final class OutboxEntityMapper {
  private OutboxEntityMapper() {}

  public static Outbox toDomain(OutboxEntity row) {
    return row == null
        ? null
        : new Outbox(row.id(), row.eventType(), row.aggregateId(), row.payload(), row.attempts());
  }

  public static OutboxEntity toEntity(Outbox model) {
    return model == null
        ? null
        : new OutboxEntity(
            model.id(), model.eventType(), model.aggregateId(), model.payload(), model.attempts());
  }
}
