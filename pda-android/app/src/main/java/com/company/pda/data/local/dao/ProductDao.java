package com.company.pda.data.local.dao;

import androidx.room.*;
import com.company.pda.data.local.entity.ProductCache;

@Dao
public interface ProductDao {
  @Query("SELECT * FROM product_cache WHERE barcode=:barcode")
  ProductCache product(String barcode);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void cache(ProductCache row);
}
