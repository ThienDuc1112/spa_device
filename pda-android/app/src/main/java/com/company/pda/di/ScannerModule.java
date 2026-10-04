package com.company.pda.di;

import android.content.Context;
import com.company.scanner.api.*;
import com.company.scanner.factory.ScannerFactory;

public final class ScannerModule {
  public static ScannerManager provide(Context context) {
    var prefs = context.getSharedPreferences("scanner", Context.MODE_PRIVATE);
    ScannerType type;
    try {
      type = ScannerType.valueOf(prefs.getString("type", "KEYBOARD"));
    } catch (Exception e) {
      type = ScannerType.KEYBOARD;
    }
    String permission = prefs.getString("permission", "");
    var defaults = ScannerConfig.defaults(type);
    String action = prefs.getString("action", defaults.action());
    String extra = prefs.getString("extra", defaults.dataExtra());
    // Older Zebra EMDK settings allowed empty intent fields.
    if (type == ScannerType.ZEBRA) {
      if (action.isBlank()) action = defaults.action();
      if (extra.isBlank()) extra = defaults.dataExtra();
    }
    var config = new ScannerConfig(type, action, extra, permission.isBlank() ? null : permission);
    return ScannerFactory.forAndroid(context).create(config);
  }
}
