package com.company.pda.data.repository;

import static com.company.pda.common.util.ApiCalls.execute;

import com.company.pda.common.exception.ApiException;
import com.company.pda.common.result.Result;
import com.company.pda.data.local.dao.ProductDao;
import com.company.pda.data.local.entity.ProductCache;
import com.company.pda.data.mapper.ProductMapper;
import com.company.pda.data.remote.api.ProductApi;
import com.company.pda.domain.model.Product;
import com.company.pda.domain.repository.ProductRepository;

public class ProductRepositoryImpl implements ProductRepository {
  private final ProductApi api;
  private final ProductDao dao;
  private final ProductMapper mapper = new ProductMapper();

  public ProductRepositoryImpl(ProductApi api, ProductDao dao) {
    this.api = api;
    this.dao = dao;
  }

  public Result<Product> get(String barcode) throws java.io.IOException {
    try {
      var product = mapper.toDomain(execute(api.product(barcode)));
      dao.cache(ProductCache.from(product));
      return new Result<>(product, false);
    } catch (ApiException e) {
      throw e;
    } catch (java.io.IOException e) {
      var row = dao.product(barcode);
      if (row != null) return new Result<>(row.product(), true);
      throw e;
    }
  }
}
