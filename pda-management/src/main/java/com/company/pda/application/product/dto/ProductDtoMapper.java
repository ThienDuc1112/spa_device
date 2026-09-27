package com.company.pda.application.product.dto;

import com.company.pda.domain.product.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductDtoMapper {
  ProductResult toDto(Product product);
}
