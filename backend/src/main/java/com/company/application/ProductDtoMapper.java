package com.company.application;

import com.company.application.dto.Contracts.ProductDto;
import com.company.domain.Models.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductDtoMapper {
  ProductDto toDto(Product product);
}
