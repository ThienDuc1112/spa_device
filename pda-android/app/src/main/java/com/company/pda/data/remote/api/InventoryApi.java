package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.inventory.InventoryDto;

public final class InventoryApi {
  private final ApiCalls api;

  public InventoryApi(ApiCalls api) {
    this.api = api;
  }

  public ApiCalls.Request<InventoryDto> inventory(String code) {
    return api.request("GET", "inventories/" + ApiCalls.path(code), null, InventoryDto.class);
  }

  public ApiCalls.Request<InventoryDto> adjust(InventoryDto.Adjustment body) {
    return api.request("POST", "inventory-adjustments", body, InventoryDto.class);
  }
}
