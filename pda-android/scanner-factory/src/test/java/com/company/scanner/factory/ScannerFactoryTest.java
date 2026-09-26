package com.company.scanner.factory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import com.company.scanner.api.*;
import java.util.*;
import org.junit.Test;

public class ScannerFactoryTest {
  @Test
  public void allManufacturerProvidersUseBroadcastOutput() {
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    var factory = ScannerFactory.forAndroid(context);
    for (ScannerType type :
        List.of(ScannerType.ZEBRA, ScannerType.UROVO, ScannerType.HONEYWELL, ScannerType.INTENT)) {
      var manager = factory.create(ScannerConfig.defaults(type));
      assertTrue(type.toString(), manager.capabilities().contains(ScannerCapability.INTENT_OUTPUT));
    }
  }

  @Test
  public void resolvesOnlyTheMatchingProvider() {
    var manager =
        new ScannerManager() {
          public void start(ScanCallback c) {}

          public void stop() {}

          public Set<ScannerCapability> capabilities() {
            return Set.of(ScannerCapability.INTENT_OUTPUT);
          }
        };
    var provider =
        new ScannerProvider() {
          public boolean supports(ScannerType type) {
            return type == ScannerType.ZEBRA;
          }

          public ScannerManager create(ScannerConfig config) {
            return manager;
          }
        };
    var factory = new ScannerFactory(List.of(provider));
    assertSame(manager, factory.create(new ScannerConfig(ScannerType.ZEBRA, "scan", "data", null)));
    assertThrows(IllegalArgumentException.class, () -> factory.create(ScannerConfig.keyboard()));
  }
}
