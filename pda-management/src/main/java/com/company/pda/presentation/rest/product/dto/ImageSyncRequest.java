package com.company.pda.presentation.rest.product.dto;

import com.company.pda.application.product.dto.ImageSyncCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class ImageSyncRequest {
  private final @NotBlank @Size(max = 50) String productCode;
  private final @Size(max = 1000) @Pattern(regexp = "https://[^\\s]+") String imageUrl;
  private final @Positive long sourceVersion;

  @java.beans.ConstructorProperties({"productCode", "imageUrl", "sourceVersion"})
  public ImageSyncRequest(String productCode, String imageUrl, long sourceVersion) {
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

  public ImageSyncCommand toCommand() {
    return new ImageSyncCommand(productCode, imageUrl, sourceVersion);
  }
}
