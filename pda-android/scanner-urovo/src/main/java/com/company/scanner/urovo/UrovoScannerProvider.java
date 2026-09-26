package com.company.scanner.urovo;

import android.content.Context;
import com.company.scanner.api.*;

public class UrovoScannerProvider implements ScannerProvider {
  private final Context context;

  public UrovoScannerProvider(Context context) {
    this.context = context;
  }

  public boolean supports(ScannerType type) {
    return type == ScannerType.UROVO;
  }

  public ScannerManager create(ScannerConfig config) {
    if (!supports(config.type())) throw new IllegalArgumentException("Wrong scanner provider");
    return new UrovoScannerManager(context, config);
  }
}
