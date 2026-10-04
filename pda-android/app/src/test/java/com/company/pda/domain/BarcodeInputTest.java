package com.company.pda.domain;

import static org.junit.Assert.*;

import com.company.pda.common.util.BarcodeInput;
import org.junit.Test;

public class BarcodeInputTest {

  @Test
  public void preservesLeadingZerosAndTrimsScannerWhitespace() {
    assertEquals("00123", BarcodeInput.normalize(" 00123\r\n"));
  }

  @Test
  public void rejectsEmptyScannerPayload() {
    assertThrows(IllegalArgumentException.class, () -> BarcodeInput.normalize("  "));
  }

  @Test
  public void rejectsOversizedPayload() {
    assertThrows(IllegalArgumentException.class, () -> BarcodeInput.normalize("1".repeat(51)));
  }
}
