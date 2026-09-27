package com.company.pda.presentation.rest.product.dto;

import com.company.pda.application.product.dto.ImageSyncCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record ImageSyncRequest(
    @NotBlank @Size(max = 50) String productCode,
    @Size(max = 1000) @Pattern(regexp = "https://[^\\s]+") String imageUrl,
    @Positive long sourceVersion) {
  public ImageSyncCommand toCommand() {
    return new ImageSyncCommand(productCode, imageUrl, sourceVersion);
  }
}
