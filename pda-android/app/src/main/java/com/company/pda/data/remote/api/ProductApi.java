package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.product.ProductDto;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ProductApi {
  private final ApiCalls api;
  private final Executor executor;

  public ProductApi(ApiCalls api, Executor executor) {
    this.api = api;
    this.executor = executor;
  }

  public CompletableFuture<ProductDto> product(String barcode) {
    return api.callApi(
        "GET",
        "products/barcode/" + ApiCalls.path(barcode),
        null,
        ProductDto.class,
        Map.of(),
        executor);
  }
}
