package com.company.pda.domain.product.repository;

import com.company.pda.domain.product.model.Product;

public interface ProductRepository {
  Product barcode(String barcode);

  Product code(String code);

  int image(long id, String url, long version);

  int imageLog(long id, String url, long version, String status, long actorId);
}
