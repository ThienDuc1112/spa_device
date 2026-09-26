package com.company.pda.data.remote.api;

import com.company.pda.data.remote.dto.inventory.InventoryDto;
import retrofit2.Call;
import retrofit2.http.*;

public interface InventoryApi {
  @GET("inventories/{code}")
  Call<InventoryDto> inventory(@Path("code") String code);

  @POST("inventory-adjustments")
  Call<InventoryDto> adjust(@Body InventoryDto.Adjustment body);
}
