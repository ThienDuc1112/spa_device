package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.infrastructure.persistence.mybatis.entity.ProductEntity;
import org.apache.ibatis.annotations.Param;

public interface ProductMapper {
  ProductEntity barcode(String barcode);

  ProductEntity code(String code);

  int image(@Param("id") long id, @Param("url") String url, @Param("version") long version);

  int imageLog(
      @Param("id") long id,
      @Param("url") String url,
      @Param("version") long version,
      @Param("status") String status,
      @Param("actorId") long actorId);
}
