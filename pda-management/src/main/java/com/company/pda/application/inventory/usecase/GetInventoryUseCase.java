package com.company.pda.application.inventory.usecase;

import com.company.pda.domain.inventory.model.Inventory;

public interface GetInventoryUseCase {
  Inventory get(String code);
}
