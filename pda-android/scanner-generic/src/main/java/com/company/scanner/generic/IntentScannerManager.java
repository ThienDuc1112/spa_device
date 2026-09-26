package com.company.scanner.generic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.content.ContextCompat;
import com.company.scanner.api.*;
import java.util.Objects;
import java.util.Set;

/** Foreground broadcast session. Call start/stop on the main thread with the screen lifecycle. */
public class IntentScannerManager implements ScannerManager {
  protected final Context context;
  private final ScannerConfig config;
  private BarcodeReceiver receiver;

  public IntentScannerManager(Context context, ScannerConfig config) {
    this.context = context.getApplicationContext();
    this.config = Objects.requireNonNull(config);
  }

  @Override
  public void start(ScanCallback callback) {
    Objects.requireNonNull(callback, "callback");
    stop();
    if (config.action() == null
        || config.action().isBlank()
        || config.dataExtra() == null
        || config.dataExtra().isBlank()) {
      callback.onError("Configure scanner action and payload key");
      return;
    }
    BarcodeReceiver candidate = new BarcodeReceiver(config, callback);
    IntentFilter filter = new IntentFilter(config.action());
    filter.addCategory(Intent.CATEGORY_DEFAULT);
    try {
      ContextCompat.registerReceiver(
          context,
          candidate,
          filter,
          config.senderPermission(),
          null,
          ContextCompat.RECEIVER_EXPORTED);
      receiver = candidate;
    } catch (RuntimeException error) {
      candidate.deactivate();
      callback.onError("Cannot register barcode receiver: " + error.getMessage());
    }
  }

  @Override
  public void stop() {
    BarcodeReceiver current = receiver;
    receiver = null;
    if (current != null) {
      current.deactivate();
      context.unregisterReceiver(current);
    }
  }

  protected final boolean isStarted() {
    return receiver != null;
  }

  @Override
  public Set<ScannerCapability> capabilities() {
    return Set.of(ScannerCapability.INTENT_OUTPUT);
  }
}
