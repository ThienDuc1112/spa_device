package com.company.pda.domain.usecase;

import com.company.pda.domain.repository.ProductRepository;

public final class GetProductUseCase {
  private final ProductRepository repository;

  public GetProductUseCase(ProductRepository repository) {
    this.repository = repository;
  }

  public com.company.pda.common.result.Result<com.company.pda.domain.model.Product> execute(
      String barcode) throws java.io.IOException {
    return repository.get(barcode);
  }
}
