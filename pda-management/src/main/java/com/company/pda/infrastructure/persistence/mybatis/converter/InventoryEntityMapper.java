package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.inventory.model.Inventory;
import com.company.pda.infrastructure.persistence.mybatis.entity.InventoryEntity;

public final class InventoryEntityMapper {
  private InventoryEntityMapper() {}

  public static Inventory toDomain(InventoryEntity row) {
    return row == null
        ? null
        : new Inventory(row.id(), row.productId(), row.storeId(), row.quantity(), row.version());
  }

  public static InventoryEntity toEntity(Inventory model) {
    return model == null
        ? null
        : new InventoryEntity(
            model.id(), model.productId(), model.storeId(), model.quantity(), model.version());
  }
}
