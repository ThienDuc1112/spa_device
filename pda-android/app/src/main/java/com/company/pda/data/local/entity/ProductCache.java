package com.company.pda.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(tableName = "product_cache")
public class ProductCache {

  @PrimaryKey @NonNull public String barcode = "";

  public String productCode, productName, imageUrl;

  public long savedAt;

  public com.company.pda.domain.model.Product product() {

    var p = new com.company.pda.domain.model.Product();

    p.barcode = barcode;

    p.productCode = productCode;

    p.productName = productName;

    p.imageUrl = imageUrl;

    return p;
  }

  public static ProductCache from(com.company.pda.domain.model.Product p) {

    var row = new ProductCache();

    row.barcode = p.barcode;

    row.productCode = p.productCode;

    row.productName = p.productName;

    row.imageUrl = p.imageUrl;

    row.savedAt = System.currentTimeMillis();

    return row;
  }
}
