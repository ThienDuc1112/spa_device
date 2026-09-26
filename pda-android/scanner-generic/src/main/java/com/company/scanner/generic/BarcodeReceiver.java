package com.company.scanner.generic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.company.scanner.api.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Shared receiver for manufacturer wedges. Business validation belongs to the ViewModel/use case.
 */
public final class BarcodeReceiver extends BroadcastReceiver {
  private final ScannerConfig config;
  private ScanCallback callback;

  public BarcodeReceiver(ScannerConfig config, ScanCallback callback) {
    this.config = Objects.requireNonNull(config);
    this.callback = Objects.requireNonNull(callback);
  }

  public void deactivate() {
    callback = null;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (callback == null || intent == null || !config.action().equals(intent.getAction())) return;
    ScanResult result;
    try {
      Bundle extras = intent.getExtras();
      if (extras == null) return;
      Object payload = extras.get(config.dataExtra());
      // Older Urovo firmware supplies raw bytes instead of barcode_string.
      if (payload == null
          && config.type() == ScannerType.UROVO
          && "barcode_string".equals(config.dataExtra())) payload = extras.get("barcode");
      String value;
      if (payload instanceof String text) {
        value = text;
      } else if (payload instanceof byte[] bytes) {
        int length = bytes.length;
        if (config.type() == ScannerType.UROVO && extras.containsKey("length")) {
          Object supplied = extras.get("length");
          if (!(supplied instanceof Integer count) || count < 0 || count > bytes.length) return;
          length = count;
        }
        value = new String(bytes, 0, length, StandardCharsets.UTF_8);
      } else {
        return;
      }
      if (value.isBlank()) return;
      String typeKey =
          switch (config.type()) {
            case ZEBRA -> "com.symbol.datawedge.label_type";
            case UROVO -> "barcodeType";
            case HONEYWELL -> "codeId";
            default -> "symbology";
          };
      Object symbology = extras.get(typeKey);
      String label =
          symbology instanceof String text && !text.isBlank()
              ? text
              : symbology instanceof Number number ? number.toString() : "UNKNOWN";
      result = new ScanResult(value, label);
    } catch (RuntimeException malformedPayload) {
      // Ignore malformed external parcels without swallowing errors in consumer code.
      return;
    }
    if (callback != null) callback.onScan(result);
  }
}
