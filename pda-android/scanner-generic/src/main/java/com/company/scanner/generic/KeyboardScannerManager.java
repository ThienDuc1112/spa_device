package com.company.scanner.generic;

import com.company.scanner.api.*;
import java.util.Set;

/** Keyboard input is delivered by the focused UI field, not a receiver. */
public class KeyboardScannerManager implements ScannerManager {
  public void start(ScanCallback callback) {}

  public void stop() {}

  public Set<ScannerCapability> capabilities() {
    return Set.of(ScannerCapability.KEYBOARD_INPUT);
  }
}
