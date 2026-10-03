package com.company.pda.presentation.rest.disposal.dto;

import com.company.pda.application.disposal.dto.DisposalLineCommand;
import java.math.BigDecimal;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DisposalLineRequest {
  private final @NotBlank @Size(max = 50) String productCode;
  private final @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(
      integer = 16,
      fraction = 2) BigDecimal quantity;
  private final @NotBlank @Size(max = 500) String reason;

  @java.beans.ConstructorProperties({"productCode", "quantity", "reason"})
  public DisposalLineRequest(String productCode, BigDecimal quantity, String reason) {
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

  public DisposalLineCommand toCommand() {
    return new DisposalLineCommand(productCode, quantity, reason);
  }
}
