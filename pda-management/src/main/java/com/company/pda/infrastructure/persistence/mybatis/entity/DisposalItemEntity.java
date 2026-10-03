package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.math.BigDecimal;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DisposalItemEntity {
  private final long productId;
  private final BigDecimal quantity;
  private final String reason;

  @java.beans.ConstructorProperties({"productId", "quantity", "reason"})
  public DisposalItemEntity(long productId, BigDecimal quantity, String reason) {
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
