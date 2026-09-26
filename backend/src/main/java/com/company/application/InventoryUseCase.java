package com.company.application;

import com.company.application.dto.Contracts.Adjustment;
import com.company.domain.Models.Inventory;

public interface InventoryUseCase {
  Inventory get(String code);

  Inventory adjust(Adjustment body);
}
