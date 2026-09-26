package com.company.scanner.generic;

import android.content.Context;
import com.company.scanner.api.*;
import com.google.mlkit.vision.codescanner.*;
import java.util.Set;

public class CameraScannerManager implements ScannerManager {
  private final GmsBarcodeScanner scanner;
  private ScanCallback callback;
  private boolean scanning;
  private ScanResult pending;

  public CameraScannerManager(Context context) {
    scanner =
        GmsBarcodeScanning.getClient(
            context, new GmsBarcodeScannerOptions.Builder().enableAutoZoom().build());
  }

  public void start(ScanCallback callback) {
    this.callback = callback;
    if (pending != null) {
      var result = pending;
      pending = null;
      callback.onScan(result);
    }
  }

  public void stop() {
    callback = null;
  }

  public Set<ScannerCapability> capabilities() {
    return Set.of(ScannerCapability.CAMERA, ScannerCapability.SOFTWARE_TRIGGER);
  }

  public void trigger() {
    if (callback == null || scanning) return;
    scanning = true;
    scanner
        .startScan()
        .addOnSuccessListener(
            code -> {
              if (code.getRawValue() != null) {
                var result = new ScanResult(code.getRawValue(), Integer.toString(code.getFormat()));
                if (callback != null) callback.onScan(result);
                else pending = result;
              }
            })
        .addOnFailureListener(
            error -> {
              if (callback != null)
                callback.onError(
                    "Camera scanner unavailable. Check Google Play services or use keyboard"
                        + " input.");
            })
        .addOnCompleteListener(task -> scanning = false);
  }
}
