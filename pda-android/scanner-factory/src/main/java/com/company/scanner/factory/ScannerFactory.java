package com.company.scanner.factory;

import android.content.Context;
import com.company.scanner.api.*;
import com.company.scanner.generic.GenericScannerProvider;
import com.company.scanner.urovo.UrovoScannerProvider;
import com.company.scanner.zebra.ZebraScannerProvider;
import java.util.List;

public final class ScannerFactory {
  private final List<ScannerProvider> providers;

  public ScannerFactory(List<ScannerProvider> providers) {
    this.providers = List.copyOf(providers);
  }

  public static ScannerFactory forAndroid(Context context) {
    return new ScannerFactory(
        List.of(
            new ZebraScannerProvider(context),
            new UrovoScannerProvider(context),
            new GenericScannerProvider(context)));
  }

  public ScannerManager create(ScannerConfig config) {
    return providers.stream()
        .filter(p -> p.supports(config.type()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No scanner provider for " + config.type()))
        .create(config);
  }
}
