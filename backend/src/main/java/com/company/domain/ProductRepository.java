package com.company.domain;

import com.company.domain.Models.*;

public interface ProductRepository {
  Product barcode(String barcode);

  Product code(String code);

  int image(long id, String url, long version);

  int imageLog(long id, String url, long version, String status, long actorId);
}
