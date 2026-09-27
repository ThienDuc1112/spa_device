package com.company.pda.application.product.usecase;

import com.company.pda.application.product.dto.ProductResult;

public interface GetProductByBarcodeUseCase {
  ProductResult lookup(String barcode);
}
