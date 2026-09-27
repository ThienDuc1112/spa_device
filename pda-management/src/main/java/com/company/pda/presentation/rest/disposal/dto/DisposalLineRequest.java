package com.company.pda.presentation.rest.disposal.dto;

import com.company.pda.application.disposal.dto.DisposalLineCommand;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public record DisposalLineRequest(
    @NotBlank @Size(max = 50) String productCode,
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 16, fraction = 2)
        BigDecimal quantity,
    @NotBlank @Size(max = 500) String reason) {
  public DisposalLineCommand toCommand() {
    return new DisposalLineCommand(productCode, quantity, reason);
  }
}
