package com.company.pda.domain.disposal.model;

import java.math.BigDecimal;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DisposalItem {
  private final long productId;
  private final BigDecimal quantity;
  private final String reason;

  @java.beans.ConstructorProperties({"productId", "quantity", "reason"})
  public DisposalItem(long productId, BigDecimal quantity, String reason) {
    this.productId = productId;
    this.quantity = quantity;
    this.reason = reason;
  }

  public long productId() {
    return productId;
  }

  public long getProductId() {
    return productId;
  }

  public BigDecimal quantity() {
    return quantity;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public String reason() {
    return reason;
  }

  public String getReason() {
    return reason;
  }
}
