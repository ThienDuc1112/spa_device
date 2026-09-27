package com.company.pda.application.inventory.usecase;

import com.company.pda.application.inventory.dto.AdjustInventoryCommand;
import com.company.pda.domain.inventory.model.Inventory;

public interface AdjustInventoryUseCase {
  Inventory adjust(AdjustInventoryCommand body);
}
