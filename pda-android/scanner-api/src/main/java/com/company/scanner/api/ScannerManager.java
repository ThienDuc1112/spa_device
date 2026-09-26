package com.company.scanner.api;

import java.util.Set;

public interface ScannerManager {
  void start(ScanCallback callback);

  void stop();

  Set<ScannerCapability> capabilities();

  default void trigger() {}
}
