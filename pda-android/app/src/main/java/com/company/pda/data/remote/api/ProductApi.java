package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.product.ProductDto;

public final class ProductApi {
  private final ApiCalls api;

  public ProductApi(ApiCalls api) {
    this.api = api;
  }

  public ApiCalls.Request<ProductDto> product(String barcode) {
    return api.request("GET", "products/barcode/" + ApiCalls.path(barcode), null, ProductDto.class);
  }
}
