package com.company.pda.data.mapper;

import com.company.pda.data.remote.dto.inventory.InventoryDto;
import com.company.pda.domain.model.Inventory;

public final class InventoryMapper {
  public Inventory toDomain(InventoryDto dto) {
    var result = new Inventory();
    result.id = dto.id;
    result.productId = dto.productId;
    result.storeId = dto.storeId;
    result.quantity = dto.quantity;
    result.version = dto.version;
    return result;
  }
}
