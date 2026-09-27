package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.auth.model.Refresh;
import com.company.pda.infrastructure.persistence.mybatis.entity.RefreshEntity;

public final class RefreshEntityMapper {
  private RefreshEntityMapper() {}

  public static Refresh toDomain(RefreshEntity row) {
    return row == null
        ? null
        : new Refresh(
            row.id(),
            row.familyId(),
            row.userId(),
            row.tokenHash(),
            row.expiresAt(),
            row.consumedAt(),
            row.revoked());
  }

  public static RefreshEntity toEntity(Refresh model) {
    return model == null
        ? null
        : new RefreshEntity(
            model.id(),
            model.familyId(),
            model.userId(),
            model.tokenHash(),
            model.expiresAt(),
            model.consumedAt(),
            model.revoked());
  }
}
