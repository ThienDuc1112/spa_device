package com.company.pda.domain.usecase;

import com.company.pda.domain.repository.InventoryRepository;

public final class AdjustInventoryUseCase {
  private final InventoryRepository repository;

  public AdjustInventoryUseCase(InventoryRepository repository) {
    this.repository = repository;
  }

  public com.company.pda.domain.model.Inventory execute(
      String id, String code, java.math.BigDecimal quantity, long version, String reason)
      throws java.io.IOException {
    return repository.adjust(id, code, quantity, version, reason);
  }
}
