package com.company.pda.presentation.rest.inventory.dto;

import com.company.pda.application.inventory.dto.AdjustInventoryCommand;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public record AdjustInventoryRequest(
    @NotNull UUID requestId,
    @NotBlank @Size(max = 50) String productCode,
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 2) BigDecimal quantity,
    @NotNull @PositiveOrZero Long version,
    @NotBlank @Size(max = 1000) String reason) {
  public AdjustInventoryCommand toCommand() {
    return new AdjustInventoryCommand(requestId, productCode, quantity, version, reason);
  }
}
