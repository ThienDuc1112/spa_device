package com.company.scanner.generic;

import android.content.Context;
import com.company.scanner.api.*;

public class GenericScannerProvider implements ScannerProvider {
  private final Context context;

  public GenericScannerProvider(Context context) {
    this.context = context;
  }

  public boolean supports(ScannerType type) {
    return type == ScannerType.KEYBOARD
        || type == ScannerType.INTENT
        || type == ScannerType.HONEYWELL
        || type == ScannerType.CAMERA;
  }

  public ScannerManager create(ScannerConfig config) {
    return switch (config.type()) {
      case KEYBOARD -> new KeyboardScannerManager();
      case CAMERA -> new CameraScannerManager(context);
      case INTENT, HONEYWELL -> new IntentScannerManager(context, config);
      default -> throw new IllegalArgumentException("Unsupported generic scanner");
    };
  }
}
