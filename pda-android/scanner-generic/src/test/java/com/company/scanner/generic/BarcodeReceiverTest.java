package com.company.scanner.generic;

import static org.mockito.Mockito.*;

import android.content.Intent;
import android.os.Bundle;
import com.company.scanner.api.*;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class BarcodeReceiverTest {
  private final ScanCallback callback = mock(ScanCallback.class);

  private Intent intent(ScannerConfig config, Object data, String typeKey, Object type) {
    Intent intent = mock(Intent.class);
    Bundle extras = mock(Bundle.class);
    when(intent.getAction()).thenReturn(config.action());
    when(intent.getExtras()).thenReturn(extras);
    when(extras.get(config.dataExtra())).thenReturn(data);
    when(extras.get(typeKey)).thenReturn(type);
    return intent;
  }

  @Test
  public void deliversEachManufacturerAndCustomIntent() {
    ScannerType[] types = {
      ScannerType.ZEBRA, ScannerType.UROVO, ScannerType.HONEYWELL, ScannerType.INTENT
    };
    String[] keys = {"com.symbol.datawedge.label_type", "barcodeType", "codeId", "symbology"};
    Object[] labels = {"LABEL-TYPE-EAN13", (byte) 8, "d", "EAN13"};
    for (int i = 0; i < types.length; i++) {
      ScannerConfig config = ScannerConfig.defaults(types[i]);
      new BarcodeReceiver(config, callback)
          .onReceive(null, intent(config, "123", keys[i], labels[i]));
      verify(callback).onScan(new ScanResult("123", labels[i].toString()));
    }
  }

  @Test
  public void respectsCustomActionAndPayloadKey() {
    var config = new ScannerConfig(ScannerType.UROVO, "custom.scan", "custom.data", null);
    var receiver = new BarcodeReceiver(config, callback);
    var data = intent(config, "ABC", "barcodeType", null);
    when(data.getAction()).thenReturn("wrong.action");
    receiver.onReceive(null, data);
    verifyNoInteractions(callback);
    when(data.getAction()).thenReturn("custom.scan");
    receiver.onReceive(null, data);
    verify(callback).onScan(new ScanResult("ABC", "UNKNOWN"));
  }

  @Test
  public void decodesUrovoBytesUsingReportedLengthAndRejectsInvalidLength() {
    var config = ScannerConfig.defaults(ScannerType.UROVO);
    var receiver = new BarcodeReceiver(config, callback);
    var data = intent(config, null, "barcodeType", (byte) 8);
    var extras = data.getExtras();
    when(extras.get("barcode")).thenReturn("ABCpadding".getBytes(StandardCharsets.UTF_8));
    when(extras.containsKey("length")).thenReturn(true);
    when(extras.get("length")).thenReturn(3);
    receiver.onReceive(null, data);
    verify(callback).onScan(new ScanResult("ABC", "8"));
    clearInvocations(callback);
    for (Object length : new Object[] {-1, 99, "3", 0}) {
      when(extras.get("length")).thenReturn(length);
      receiver.onReceive(null, data);
    }
    verifyNoInteractions(callback);
  }

  @Test
  public void leavesLengthValidationToUseCaseAndIgnoresLateDelivery() {
    var config = ScannerConfig.defaults(ScannerType.ZEBRA);
    var receiver = new BarcodeReceiver(config, callback);
    String barcode = "A".repeat(100);
    var data = intent(config, barcode, "unused", null);
    receiver.onReceive(null, data);
    verify(callback).onScan(new ScanResult(barcode, "UNKNOWN"));
    clearInvocations(callback);
    receiver.deactivate();
    receiver.onReceive(null, data);
    verifyNoInteractions(callback);
  }

  @Test
  public void ignoresMissingMalformedAndBlankPayloads() {
    var config = ScannerConfig.defaults(ScannerType.INTENT);
    var receiver = new BarcodeReceiver(config, callback);
    receiver.onReceive(null, null);
    for (Object payload : new Object[] {null, 42, "", "  "}) {
      receiver.onReceive(null, intent(config, payload, "unused", null));
    }
    var malformed = intent(config, "ABC", "unused", null);
    when(malformed.getExtras()).thenThrow(new IllegalArgumentException("bad parcel"));
    receiver.onReceive(null, malformed);
    verifyNoInteractions(callback);
  }
}
