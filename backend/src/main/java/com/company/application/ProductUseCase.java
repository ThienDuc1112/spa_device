package com.company.application;

import com.company.application.dto.Contracts.*;

public interface ProductUseCase {
  ProductDto lookup(String barcode);

  void sync(ImageSync body);
}
