package com.company.pda.application.inventory.usecase;

public interface InventoryUseCase extends GetInventoryUseCase, AdjustInventoryUseCase {
  java.util.List<java.util.Map<String, Object>> history(long afterId);
}
