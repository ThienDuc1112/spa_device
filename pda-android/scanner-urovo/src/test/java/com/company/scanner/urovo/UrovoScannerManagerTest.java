package com.company.scanner.urovo;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.company.scanner.api.*;
import org.junit.Test;

public class UrovoScannerManagerTest {
  private final Context context = mock(Context.class);

  private UrovoScannerManager manager() {
    when(context.getApplicationContext()).thenReturn(context);
    return new UrovoScannerManager(context, new UrovoScannerConfig().toScannerConfig());
  }

  @Test
  public void sendsUrovoCommandsOnlyDuringActiveSession() {
    try (var compat = mockStatic(ContextCompat.class);
        var intents =
            mockConstruction(
                Intent.class,
                withSettings().defaultAnswer(RETURNS_SELF),
                (intent, construction) ->
                    assertEquals(
                        "com.ubx.datawedge.api.ACTION", construction.arguments().get(0)))) {
      var manager = manager();
      assertTrue(manager.capabilities().contains(ScannerCapability.INTENT_OUTPUT));
      assertTrue(manager.capabilities().contains(ScannerCapability.SOFTWARE_TRIGGER));
      manager.trigger();
      manager.stop();
      assertTrue(intents.constructed().isEmpty());
      manager.start(mock(ScanCallback.class));
      assertTrue(intents.constructed().isEmpty());
      manager.trigger();
      manager.stop();
      manager.stop();
      manager.trigger();
      assertEquals(2, intents.constructed().size());
      var start = intents.constructed().get(0);
      var stop = intents.constructed().get(1);
      verify(start).putExtra("SOFT_SCAN_TRIGGER", "START_SCANNING");
      verify(stop).putExtra("SOFT_SCAN_TRIGGER", "STOP_SCANNING");
      verify(context).sendBroadcast(start);
      verify(context).sendBroadcast(stop);
      verify(context).unregisterReceiver(any());
    }
  }

  @Test
  public void releasesReceiverEvenWhenStopCommandFails() {
    try (var compat = mockStatic(ContextCompat.class);
        var intents = mockConstruction(Intent.class, withSettings().defaultAnswer(RETURNS_SELF))) {
      var manager = manager();
      manager.start(mock(ScanCallback.class));
      doThrow(new SecurityException("denied")).when(context).sendBroadcast(any());
      assertThrows(SecurityException.class, manager::stop);
      verify(context).unregisterReceiver(any());
      manager.stop();
      manager.trigger();
      verify(context, times(1)).sendBroadcast(any());
    }
  }

  @Test
  public void invalidSettingsDoNotEnableTrigger() {
    when(context.getApplicationContext()).thenReturn(context);
    var manager =
        new UrovoScannerManager(context, new ScannerConfig(ScannerType.UROVO, "", "", null));
    var callback = mock(ScanCallback.class);
    manager.start(callback);
    manager.trigger();
    manager.stop();
    verify(callback).onError("Configure scanner action and payload key");
    verify(context, never()).sendBroadcast(any());
  }
}
