package com.company.pda.presentation.rest.inventory.dto;

import com.company.pda.application.inventory.dto.AdjustInventoryCommand;
import java.math.BigDecimal;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class AdjustInventoryRequest {
  private final @NotNull UUID requestId;
  private final @NotBlank @Size(max = 50) String productCode;
  private final @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 2) BigDecimal quantity;
  private final @NotNull @PositiveOrZero Long version;
  private final @NotBlank @Size(max = 1000) String reason;

  @java.beans.ConstructorProperties({"requestId", "productCode", "quantity", "version", "reason"})
  public AdjustInventoryRequest(
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

  public AdjustInventoryCommand toCommand() {
    return new AdjustInventoryCommand(requestId, productCode, quantity, version, reason);
  }
}
