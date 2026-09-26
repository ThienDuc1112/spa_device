package com.company.pda.data.mapper;

import com.company.pda.data.remote.dto.product.ProductDto;
import com.company.pda.domain.model.Product;

public final class ProductMapper {
  public Product toDomain(ProductDto dto) {
    var result = new Product();
    result.barcode = dto.barcode;
    result.productCode = dto.productCode;
    result.productName = dto.productName;
    result.imageUrl = dto.imageUrl;
    return result;
  }
}
