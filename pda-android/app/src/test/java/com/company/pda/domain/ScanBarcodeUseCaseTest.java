package com.company.pda.domain;

import static org.junit.Assert.*;

import com.company.pda.domain.usecase.ScanBarcodeUseCase;
import org.junit.Test;

public class ScanBarcodeUseCaseTest {
  private final ScanBarcodeUseCase useCase = new ScanBarcodeUseCase();

  @Test
  public void preservesLeadingZerosAndTrimsScannerWhitespace() {
    assertEquals("00123", useCase.execute(" 00123\r\n", "EAN").barcode());
  }

  @Test
  public void rejectsEmptyScannerPayload() {
    assertThrows(IllegalArgumentException.class, () -> useCase.execute("  ", "UNKNOWN"));
  }

  @Test
  public void rejectsOversizedPayload() {
    assertThrows(IllegalArgumentException.class, () -> useCase.execute("1".repeat(51), "UNKNOWN"));
  }
}
