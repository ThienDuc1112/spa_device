package com.company.scanner.zebra;

import com.company.scanner.api.*;

public final class ZebraScannerConfig {
  private final String action;
  private final String dataExtra;
  private final String senderPermission;

  public ZebraScannerConfig(String action, String dataExtra, String senderPermission) {
    this.action = action;
    this.dataExtra = dataExtra;
    this.senderPermission = senderPermission;
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
    if (!(obj instanceof ZebraScannerConfig other)) return false;
    return java.util.Objects.equals(action, other.action)
        && java.util.Objects.equals(dataExtra, other.dataExtra)
        && java.util.Objects.equals(senderPermission, other.senderPermission);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(action, dataExtra, senderPermission);
  }

  @Override
  public String toString() {
    return "ZebraScannerConfig[action="
        + action
        + ", dataExtra="
        + dataExtra
        + ", senderPermission="
        + senderPermission
        + "]";
  }

  /** Match this action in the app's DataWedge profile. */
  public ZebraScannerConfig() {
    this(
        ScannerConfig.defaults(ScannerType.ZEBRA).action(),
        ScannerConfig.defaults(ScannerType.ZEBRA).dataExtra(),
        null);
  }

  public ScannerConfig toScannerConfig() {
    return new ScannerConfig(ScannerType.ZEBRA, action, dataExtra, senderPermission);
  }
}
