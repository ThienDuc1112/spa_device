package com.company.pda.infrastructure.persistence.mybatis.repository;

import com.company.pda.domain.product.model.Product;
import com.company.pda.domain.product.repository.ProductRepository;
import com.company.pda.infrastructure.persistence.mybatis.converter.ProductEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisProductRepository implements ProductRepository {
  private final ProductMapper mapper;

  public MyBatisProductRepository(ProductMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Product barcode(String barcode) {
    return ProductEntityMapper.toDomain(mapper.barcode(barcode));
  }

  @Override
  public Product code(String code) {
    return ProductEntityMapper.toDomain(mapper.code(code));
  }

  @Override
  public int image(long id, String url, long version) {
    return mapper.image(id, url, version);
  }

  @Override
  public int imageLog(long id, String url, long version, String status, long actorId) {
    return mapper.imageLog(id, url, version, status, actorId);
  }
}
