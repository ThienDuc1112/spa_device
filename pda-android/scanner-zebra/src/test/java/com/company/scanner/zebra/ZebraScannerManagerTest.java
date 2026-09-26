package com.company.scanner.zebra;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.company.scanner.api.*;
import org.junit.Test;

public class ZebraScannerManagerTest {
  @Test
  public void triggersOnlyDuringActiveSessionAndReleasesReceiver() {
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    try (var compat = mockStatic(ContextCompat.class);
        var intents =
            mockConstruction(
                Intent.class,
                withSettings().defaultAnswer(RETURNS_SELF),
                (intent, construction) ->
                    assertEquals(
                        "com.symbol.datawedge.api.ACTION", construction.arguments().get(0)))) {
      var manager = new ZebraScannerManager(context, new ZebraScannerConfig().toScannerConfig());
      assertTrue(manager.capabilities().contains(ScannerCapability.INTENT_OUTPUT));
      manager.trigger();
      assertTrue(intents.constructed().isEmpty());
      manager.start(mock(ScanCallback.class));
      manager.trigger();
      manager.stop();
      manager.stop();
      manager.trigger();
      assertEquals(2, intents.constructed().size());
      Intent start = intents.constructed().get(0);
      Intent stop = intents.constructed().get(1);
      verify(start).putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING");
      verify(stop).putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "STOP_SCANNING");
      verify(start).setPackage("com.symbol.datawedge");
      verify(context).sendBroadcast(start);
      verify(context).sendBroadcast(stop);
      verify(context).unregisterReceiver(any());
    }
  }
}
