package com.company.scanner.zebra;

import android.content.Context;
import com.company.scanner.api.*;

public class ZebraScannerProvider implements ScannerProvider {
  private final Context context;

  public ZebraScannerProvider(Context context) {
    this.context = context;
  }

  public boolean supports(ScannerType type) {
    return type == ScannerType.ZEBRA;
  }

  public ScannerManager create(ScannerConfig config) {
    if (!supports(config.type())) throw new IllegalArgumentException("Wrong scanner provider");
    return new ZebraScannerManager(context, config);
  }
}
