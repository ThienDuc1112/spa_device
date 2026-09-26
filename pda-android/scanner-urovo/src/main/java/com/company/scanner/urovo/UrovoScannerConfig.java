package com.company.scanner.urovo;

import com.company.scanner.api.*;

public record UrovoScannerConfig(String action, String dataExtra, String senderPermission) {
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
