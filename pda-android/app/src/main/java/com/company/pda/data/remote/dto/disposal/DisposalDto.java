package com.company.pda.data.remote.dto.disposal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DisposalDto {
  public String id, status, remarks, createdAt;
  public long version;

  public static class Item {
    public long productId;
    public BigDecimal quantity;
    public String reason;
  }

  public static class Detail {
    public DisposalDto disposal;
    public List<Item> items;
    public List<Map<String, Object>> history;
  }

  public static final class Line {
    private final String productCode;
    private final BigDecimal quantity;
    private final String reason;

    public Line(String productCode, BigDecimal quantity, String reason) {
      this.productCode = productCode;
      this.quantity = quantity;
      this.reason = reason;
    }

    public String productCode() {
      return productCode;
    }

    public BigDecimal quantity() {
      return quantity;
    }

    public String reason() {
      return reason;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Line other)) return false;
      return java.util.Objects.equals(productCode, other.productCode)
          && java.util.Objects.equals(quantity, other.quantity)
          && java.util.Objects.equals(reason, other.reason);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(productCode, quantity, reason);
    }

    @Override
    public String toString() {
      return "Line[productCode="
          + productCode
          + ", quantity="
          + quantity
          + ", reason="
          + reason
          + "]";
    }
  }

  public static final class Create {
    private final String requestId;
    private final String remarks;
    private final List<Line> items;

    public Create(String requestId, String remarks, List<Line> items) {
      this.requestId = requestId;
      this.remarks = remarks;
      this.items = items;
    }

    public String requestId() {
      return requestId;
    }

    public String remarks() {
      return remarks;
    }

    public List<Line> items() {
      return items;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Create other)) return false;
      return java.util.Objects.equals(requestId, other.requestId)
          && java.util.Objects.equals(remarks, other.remarks)
          && java.util.Objects.equals(items, other.items);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(requestId, remarks, items);
    }

    @Override
    public String toString() {
      return "Create[requestId=" + requestId + ", remarks=" + remarks + ", items=" + items + "]";
    }
  }

  public static final class Transition {
    private final long version;

    public Transition(long version) {
      this.version = version;
    }

    public long version() {
      return version;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Transition other)) return false;
      return java.util.Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(version);
    }

    @Override
    public String toString() {
      return "Transition[version=" + version + "]";
    }
  }
}
