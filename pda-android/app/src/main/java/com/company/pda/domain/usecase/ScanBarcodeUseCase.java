package com.company.pda.domain.usecase;

import com.company.pda.domain.model.ScanResult;

public final class ScanBarcodeUseCase {
  public ScanResult execute(String value, String symbology) {
    if (value == null || value.trim().isEmpty() || value.trim().length() > 50)
      throw new IllegalArgumentException("Barcode must contain 1–50 characters");
    return new ScanResult(value.trim(), symbology);
  }
}
