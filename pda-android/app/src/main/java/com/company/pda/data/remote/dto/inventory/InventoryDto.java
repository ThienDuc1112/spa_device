package com.company.pda.data.remote.dto.inventory;

public class InventoryDto {
  public long id, productId, storeId, version;
  public java.math.BigDecimal quantity;

  public static final class Adjustment {
    private final String requestId;
    private final String productCode;
    private final java.math.BigDecimal quantity;
    private final long version;
    private final String reason;

    public Adjustment(
        String requestId,
        String productCode,
        java.math.BigDecimal quantity,
        long version,
        String reason) {
      this.requestId = requestId;
      this.productCode = productCode;
      this.quantity = quantity;
      this.version = version;
      this.reason = reason;
    }

    public String requestId() {
      return requestId;
    }

    public String productCode() {
      return productCode;
    }

    public java.math.BigDecimal quantity() {
      return quantity;
    }

    public long version() {
      return version;
    }

    public String reason() {
      return reason;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Adjustment other)) return false;
      return java.util.Objects.equals(requestId, other.requestId)
          && java.util.Objects.equals(productCode, other.productCode)
          && java.util.Objects.equals(quantity, other.quantity)
          && java.util.Objects.equals(version, other.version)
          && java.util.Objects.equals(reason, other.reason);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(requestId, productCode, quantity, version, reason);
    }

    @Override
    public String toString() {
      return "Adjustment[requestId="
          + requestId
          + ", productCode="
          + productCode
          + ", quantity="
          + quantity
          + ", version="
          + version
          + ", reason="
          + reason
          + "]";
    }
  }
}
