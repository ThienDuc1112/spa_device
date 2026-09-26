package com.company.scanner.generic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.IntentFilter;
import androidx.core.content.ContextCompat;
import com.company.scanner.api.*;
import java.util.ArrayList;
import org.junit.Test;

public class IntentScannerManagerTest {
  @Test
  public void restartUnregistersOldReceiverAndStopIsIdempotent() {
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    ScanCallback callback = mock(ScanCallback.class);
    var registered = new ArrayList<BarcodeReceiver>();
    try (var compat = mockStatic(ContextCompat.class)) {
      compat
          .when(
              () ->
                  ContextCompat.registerReceiver(
                      eq(context),
                      any(BarcodeReceiver.class),
                      any(IntentFilter.class),
                      eq("vendor.permission"),
                      isNull(),
                      eq(ContextCompat.RECEIVER_EXPORTED)))
          .thenAnswer(
              call -> {
                registered.add(call.getArgument(1));
                return null;
              });
      var manager =
          new IntentScannerManager(
              context, new ScannerConfig(ScannerType.INTENT, "scan", "data", "vendor.permission"));
      manager.start(callback);
      manager.start(callback);
      manager.stop();
      manager.stop();
      org.junit.Assert.assertEquals(2, registered.size());
      for (var receiver : registered) verify(context).unregisterReceiver(receiver);
    }
  }

  @Test
  public void registrationFailureReportsErrorAndDoesNotUnregister() {
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    ScanCallback callback = mock(ScanCallback.class);
    try (var compat = mockStatic(ContextCompat.class)) {
      compat
          .when(
              () ->
                  ContextCompat.registerReceiver(
                      eq(context),
                      any(BarcodeReceiver.class),
                      any(IntentFilter.class),
                      isNull(),
                      isNull(),
                      eq(ContextCompat.RECEIVER_EXPORTED)))
          .thenThrow(new SecurityException("denied"));
      var manager = new IntentScannerManager(context, ScannerConfig.defaults(ScannerType.INTENT));
      manager.start(callback);
      manager.stop();
      verify(callback).onError(contains("denied"));
      verify(context, never()).unregisterReceiver(any());
    }
  }

  @Test
  public void invalidSettingsReportErrorInsteadOfCrashingScreen() {
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    ScanCallback callback = mock(ScanCallback.class);
    var manager =
        new IntentScannerManager(context, new ScannerConfig(ScannerType.ZEBRA, "", "", null));
    manager.start(callback);
    manager.stop();
    verify(callback).onError("Configure scanner action and payload key");
    verify(context, never()).unregisterReceiver(any());
  }
}
