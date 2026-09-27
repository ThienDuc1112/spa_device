package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.pdafinder.model.PdaFindRequest;
import com.company.pda.infrastructure.persistence.mybatis.entity.PdaFindRequestEntity;

public final class PdaFindRequestEntityMapper {
  private PdaFindRequestEntityMapper() {}

  public static PdaFindRequest toDomain(PdaFindRequestEntity row) {
    return row == null
        ? null
        : new PdaFindRequest(
            row.id(),
            row.requesterId(),
            row.storeId(),
            row.deviceId(),
            row.status(),
            row.expiresAt());
  }

  public static PdaFindRequestEntity toEntity(PdaFindRequest model) {
    return model == null
        ? null
        : new PdaFindRequestEntity(
            model.id(),
            model.requesterId(),
            model.storeId(),
            model.deviceId(),
            model.status(),
            model.expiresAt());
  }
}
