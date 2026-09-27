package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.disposal.model.DisposalItem;
import com.company.pda.infrastructure.persistence.mybatis.entity.DisposalItemEntity;

public final class DisposalItemEntityMapper {
  private DisposalItemEntityMapper() {}

  public static DisposalItem toDomain(DisposalItemEntity row) {
    return row == null ? null : new DisposalItem(row.productId(), row.quantity(), row.reason());
  }

  public static DisposalItemEntity toEntity(DisposalItem model) {
    return model == null
        ? null
        : new DisposalItemEntity(model.productId(), model.quantity(), model.reason());
  }
}
