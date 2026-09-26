package com.company.pda.data.repository;

import com.company.pda.domain.model.ScanResult;
import com.company.pda.domain.scanner.ScannerRepository;
import com.company.scanner.api.*;
import java.util.function.Consumer;

public class ScannerRepositoryImpl implements ScannerRepository {
  private final ScannerManager manager;

  public ScannerRepositoryImpl(ScannerManager manager) {
    this.manager = manager;
  }

  public void start(Consumer<ScanResult> onScan, Consumer<String> onError) {
    manager.start(
        new ScanCallback() {
          public void onScan(com.company.scanner.api.ScanResult result) {
            onScan.accept(new ScanResult(result.barcode(), result.symbology()));
          }

          public void onError(String message) {
            onError.accept(message);
          }
        });
  }

  public void stop() {
    manager.stop();
  }

  public void trigger() {
    manager.trigger();
  }
}
