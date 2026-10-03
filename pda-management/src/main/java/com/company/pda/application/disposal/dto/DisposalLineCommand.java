package com.company.pda.application.disposal.dto;

import java.math.BigDecimal;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DisposalLineCommand {
  private final String productCode;
  private final BigDecimal quantity;
  private final String reason;

  @java.beans.ConstructorProperties({"productCode", "quantity", "reason"})
  public DisposalLineCommand(String productCode, BigDecimal quantity, String reason) {
    this.productCode = productCode;
    this.quantity = quantity;
    this.reason = reason;
  }

  public String productCode() {
    return productCode;
  }

  public String getProductCode() {
    return productCode;
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
