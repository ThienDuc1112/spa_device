package com.company.pda.application.product.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class ProductResult {
  private final String barcode;
  private final String productCode;
  private final String productName;
  private final String imageUrl;

  @java.beans.ConstructorProperties({"barcode", "productCode", "productName", "imageUrl"})
  public ProductResult(String barcode, String productCode, String productName, String imageUrl) {
    this.barcode = barcode;
    this.productCode = productCode;
    this.productName = productName;
    this.imageUrl = imageUrl;
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
}
