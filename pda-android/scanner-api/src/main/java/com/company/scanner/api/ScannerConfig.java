package com.company.scanner.api;

public record ScannerConfig(
    ScannerType type, String action, String dataExtra, String senderPermission) {
  public ScannerConfig {
    if (type == null) throw new IllegalArgumentException("Scanner type is required");
  }

  public static ScannerConfig keyboard() {
    return new ScannerConfig(ScannerType.KEYBOARD, "", "", null);
  }

  /** Wedge profiles must be configured on the device to match these values. */
  public static ScannerConfig defaults(ScannerType type) {
    return switch (type) {
      case ZEBRA ->
          new ScannerConfig(type, "com.company.pda.SCAN", "com.symbol.datawedge.data_string", null);
      case UROVO ->
          new ScannerConfig(type, "android.intent.ACTION_DECODE_DATA", "barcode_string", null);
      case HONEYWELL, INTENT -> new ScannerConfig(type, "com.company.pda.SCAN", "data", null);
      case KEYBOARD, CAMERA -> new ScannerConfig(type, "", "", null);
    };
  }
}
