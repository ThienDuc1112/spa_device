package com.company.scanner.api;

public interface ScanCallback {
  void onScan(ScanResult result);

  default void onError(String message) {}
}
