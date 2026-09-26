package com.company.pda.domain.repository;

import com.company.pda.domain.model.*;

public interface ProductRepository {
  com.company.pda.common.result.Result<Product> get(String barcode) throws java.io.IOException;
}
