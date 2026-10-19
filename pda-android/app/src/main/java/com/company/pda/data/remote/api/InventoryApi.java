package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.inventory.InventoryDto;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class InventoryApi {
  private final ApiCalls api;
  private final Executor executor;

  public InventoryApi(ApiCalls api, Executor executor) {
    this.api = api;
    this.executor = executor;
  }

  public CompletableFuture<InventoryDto> inventory(String code) {
    return api.callApi(
        "GET", "inventories/" + ApiCalls.path(code), null, InventoryDto.class, Map.of(), executor);
  }

  public CompletableFuture<InventoryDto> adjust(InventoryDto.Adjustment body) {
    return api.callApi(
        "POST", "inventory-adjustments", body, InventoryDto.class, Map.of(), executor);
  }
}
