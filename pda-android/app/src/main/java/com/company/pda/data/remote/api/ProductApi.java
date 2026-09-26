package com.company.pda.data.remote.api;

import com.company.pda.data.remote.dto.product.ProductDto;
import retrofit2.Call;
import retrofit2.http.*;

public interface ProductApi {
  @GET("products/barcode/{barcode}")
  Call<ProductDto> product(@Path("barcode") String barcode);
}
