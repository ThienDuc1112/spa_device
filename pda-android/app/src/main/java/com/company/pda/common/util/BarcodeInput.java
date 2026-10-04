package com.company.pda.common.util;

public final class BarcodeInput {
  private BarcodeInput() {}

  public static String normalize(String value) {
    if (value == null || value.trim().isEmpty() || value.trim().length() > 50)
      throw new IllegalArgumentException("Barcode must contain 1-50 characters");
    return value.trim();
  }
}
