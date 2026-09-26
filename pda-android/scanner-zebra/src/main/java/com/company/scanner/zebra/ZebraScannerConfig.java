package com.company.scanner.zebra;

import com.company.scanner.api.*;

public record ZebraScannerConfig(String action, String dataExtra, String senderPermission) {
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
