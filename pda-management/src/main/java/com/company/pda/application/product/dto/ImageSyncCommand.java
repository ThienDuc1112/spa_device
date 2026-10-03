package com.company.pda.application.product.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class ImageSyncCommand {
  private final String productCode;
  private final String imageUrl;
  private final long sourceVersion;

  @java.beans.ConstructorProperties({"productCode", "imageUrl", "sourceVersion"})
  public ImageSyncCommand(String productCode, String imageUrl, long sourceVersion) {
    this.productCode = productCode;
    this.imageUrl = imageUrl;
    this.sourceVersion = sourceVersion;
  }

  public String productCode() {
    return productCode;
  }

  public String getProductCode() {
    return productCode;
  }

  public String imageUrl() {
    return imageUrl;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public long sourceVersion() {
    return sourceVersion;
  }

  public long getSourceVersion() {
    return sourceVersion;
  }
}
