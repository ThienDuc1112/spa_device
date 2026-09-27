package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.product.model.Product;
import com.company.pda.infrastructure.persistence.mybatis.entity.ProductEntity;

public final class ProductEntityMapper {
  private ProductEntityMapper() {}

  public static Product toDomain(ProductEntity row) {
    return row == null
        ? null
        : new Product(
            row.id(),
            row.barcode(),
            row.productCode(),
            row.productName(),
            row.imageUrl(),
            row.imageVersion());
  }

  public static ProductEntity toEntity(Product model) {
    return model == null
        ? null
        : new ProductEntity(
            model.id(),
            model.barcode(),
            model.productCode(),
            model.productName(),
            model.imageUrl(),
            model.imageVersion());
  }
}
