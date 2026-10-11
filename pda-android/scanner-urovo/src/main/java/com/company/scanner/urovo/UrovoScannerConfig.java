package com.company.scanner.urovo;

import com.company.scanner.api.*;

public final class UrovoScannerConfig {
  private final String action;
  private final String dataExtra;
  private final String senderPermission;

  public UrovoScannerConfig(String action, String dataExtra, String senderPermission) {
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
    if (!(obj instanceof UrovoScannerConfig other)) return false;
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
    return "UrovoScannerConfig[action="
        + action
        + ", dataExtra="
        + dataExtra
        + ", senderPermission="
        + senderPermission
        + "]";
  }

  public UrovoScannerConfig() {
    this(
        ScannerConfig.defaults(ScannerType.UROVO).action(),
        ScannerConfig.defaults(ScannerType.UROVO).dataExtra(),
        null);
  }

  public ScannerConfig toScannerConfig() {
    return new ScannerConfig(ScannerType.UROVO, action, dataExtra, senderPermission);
  }
}
