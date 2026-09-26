package com.company.scanner.api;

public interface ScannerProvider {
  boolean supports(ScannerType type);

  ScannerManager create(ScannerConfig config);
}
