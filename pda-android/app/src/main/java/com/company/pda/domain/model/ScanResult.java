package com.company.pda.domain.model;

public final class ScanResult {
  private final String barcode;
  private final String symbology;

  public ScanResult(String barcode, String symbology) {
    this.barcode = barcode;
    this.symbology = symbology;
  }

  public String barcode() {
    return barcode;
  }

  public String symbology() {
    return symbology;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ScanResult other)) return false;
    return java.util.Objects.equals(barcode, other.barcode)
        && java.util.Objects.equals(symbology, other.symbology);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(barcode, symbology);
  }

  @Override
  public String toString() {
    return "ScanResult[barcode=" + barcode + ", symbology=" + symbology + "]";
  }
}
