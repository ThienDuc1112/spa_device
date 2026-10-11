package com.company.scanner.api;

public final class ScannerConfig {
  private final ScannerType type;
  private final String action;
  private final String dataExtra;
  private final String senderPermission;

  public ScannerConfig(ScannerType type, String action, String dataExtra, String senderPermission) {
    if (type == null) throw new IllegalArgumentException("Scanner type is required");
    this.type = type;
    this.action = action;
    this.dataExtra = dataExtra;
    this.senderPermission = senderPermission;
  }

  public ScannerType type() {
    return type;
  }

  public String action() {
    return action;
  }

  public String dataExtra() {
    return dataExtra;
  }

  public String senderPermission() {
    return senderPermission;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ScannerConfig other)) return false;
    return java.util.Objects.equals(type, other.type)
        && java.util.Objects.equals(action, other.action)
        && java.util.Objects.equals(dataExtra, other.dataExtra)
        && java.util.Objects.equals(senderPermission, other.senderPermission);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(type, action, dataExtra, senderPermission);
  }

  @Override
  public String toString() {
    return "ScannerConfig[type="
        + type
        + ", action="
        + action
        + ", dataExtra="
        + dataExtra
        + ", senderPermission="
        + senderPermission
        + "]";
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
