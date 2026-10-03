package com.company.pda.application.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class AdjustInventoryCommand {
  private final UUID requestId;
  private final String productCode;
  private final BigDecimal quantity;
  private final Long version;
  private final String reason;

  @java.beans.ConstructorProperties({"requestId", "productCode", "quantity", "version", "reason"})
  public AdjustInventoryCommand(
      UUID requestId, String productCode, BigDecimal quantity, Long version, String reason) {
    this.requestId = requestId;
    this.productCode = productCode;
    this.quantity = quantity;
    this.version = version;
    this.reason = reason;
  }

  public UUID requestId() {
    return requestId;
  }

  public UUID getRequestId() {
    return requestId;
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

  public Long version() {
    return version;
  }

  public Long getVersion() {
    return version;
  }

  public String reason() {
    return reason;
  }

  public String getReason() {
    return reason;
  }
}
