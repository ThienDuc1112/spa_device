package com.company.pda.infrastructure.persistence.mybatis.entity;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class ProductEntity {
  private final long id;
  private final String barcode;
  private final String productCode;
  private final String productName;
  private final String imageUrl;
  private final long imageVersion;

  @java.beans.ConstructorProperties({
    "id",
    "barcode",
    "productCode",
    "productName",
    "imageUrl",
    "imageVersion"
  })
  public ProductEntity(
      long id,
      String barcode,
      String productCode,
      String productName,
      String imageUrl,
      long imageVersion) {
    this.id = id;
    this.barcode = barcode;
    this.productCode = productCode;
    this.productName = productName;
    this.imageUrl = imageUrl;
    this.imageVersion = imageVersion;
  }

  public long id() {
    return id;
  }

  public long getId() {
    return id;
  }

  public String barcode() {
    return barcode;
  }

  public String getBarcode() {
    return barcode;
  }

  public String productCode() {
    return productCode;
  }

  public String getProductCode() {
    return productCode;
  }

  public String productName() {
    return productName;
  }

  public String getProductName() {
    return productName;
  }

  public String imageUrl() {
    return imageUrl;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public long imageVersion() {
    return imageVersion;
  }

  public long getImageVersion() {
    return imageVersion;
  }
}
