package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.disposal.model.Disposal;
import com.company.pda.infrastructure.persistence.mybatis.entity.DisposalEntity;

public final class DisposalEntityMapper {
  private DisposalEntityMapper() {}

  public static Disposal toDomain(DisposalEntity row) {
    return row == null
        ? null
        : new Disposal(
            row.id(),
            row.storeId(),
            row.status(),
            row.remarks(),
            row.createdBy(),
            row.version(),
            row.createdAt());
  }

  public static DisposalEntity toEntity(Disposal model) {
    return model == null
        ? null
        : new DisposalEntity(
            model.id(),
            model.storeId(),
            model.status(),
            model.remarks(),
            model.createdBy(),
            model.version(),
            model.createdAt());
  }
}
