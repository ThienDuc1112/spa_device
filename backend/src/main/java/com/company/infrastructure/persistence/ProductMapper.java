package com.company.infrastructure.persistence;

import com.company.domain.Models.*;
import com.company.domain.ProductRepository;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProductMapper extends ProductRepository {
  @Select(
      "SELECT id,barcode,product_code,product_name,image_url,image_version FROM products WHERE"
          + " barcode=#{barcode} AND active=true")
  Product barcode(String barcode);

  @Select(
      "SELECT id,barcode,product_code,product_name,image_url,image_version FROM products WHERE"
          + " product_code=#{code} AND active=true")
  Product code(String code);

  @Update(
      "UPDATE products SET image_url=#{url},image_version=#{version},updated_at=now() WHERE"
          + " id=#{id} AND image_version<#{version}")
  int image(long id, String url, long version);

  @Insert(
      "INSERT INTO"
          + " product_image_sync_logs(product_id,image_url,source_version,sync_status,created_by)"
          + " VALUES(#{id},#{url},#{version},#{status},#{actorId})")
  int imageLog(long id, String url, long version, String status, long actorId);
}
