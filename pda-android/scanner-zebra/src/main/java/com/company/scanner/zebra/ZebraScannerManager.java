package com.company.scanner.zebra;

import android.content.Context;
import android.content.Intent;
import com.company.scanner.api.*;
import com.company.scanner.generic.IntentScannerManager;
import java.util.Set;

/** DataWedge owns the hardware; barcode results arrive through the shared BarcodeReceiver. */
public class ZebraScannerManager extends IntentScannerManager {
  public ZebraScannerManager(Context context, ScannerConfig config) {
    super(context, config);
    if (config.type() != ScannerType.ZEBRA) {
      throw new IllegalArgumentException("Zebra scanner configuration is required");
    }
  }

  @Override
  public void trigger() {
    if (isStarted()) sendTrigger("START_SCANNING");
  }

  @Override
  public void stop() {
    try {
      if (isStarted()) sendTrigger("STOP_SCANNING");
    } finally {
      super.stop();
    }
  }

  private void sendTrigger(String command) {
    context.sendBroadcast(
        new Intent("com.symbol.datawedge.api.ACTION")
            .setPackage("com.symbol.datawedge")
            .putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", command));
  }

  @Override
  public Set<ScannerCapability> capabilities() {
    return Set.of(ScannerCapability.INTENT_OUTPUT, ScannerCapability.SOFTWARE_TRIGGER);
  }
}
