package com.company.scanner.urovo;

import android.content.Context;
import android.content.Intent;
import com.company.scanner.api.ScannerCapability;
import com.company.scanner.api.ScannerConfig;
import com.company.scanner.api.ScannerType;
import com.company.scanner.generic.IntentScannerManager;
import java.util.Set;

/** ScanWedge Intent API adapter; requires compatible ScanWedge firmware for software triggering. */
public class UrovoScannerManager extends IntentScannerManager {
  public UrovoScannerManager(Context context, ScannerConfig config) {
    super(context, config);
    if (config.type() != ScannerType.UROVO) {
      throw new IllegalArgumentException("Urovo scanner configuration is required");
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
    // Urovo IntentKeys: the extra is unprefixed, unlike Zebra's DataWedge protocol.
    context.sendBroadcast(
        new Intent("com.ubx.datawedge.api.ACTION").putExtra("SOFT_SCAN_TRIGGER", command));
  }

  @Override
  public Set<ScannerCapability> capabilities() {
    return Set.of(ScannerCapability.INTENT_OUTPUT, ScannerCapability.SOFTWARE_TRIGGER);
  }
}
