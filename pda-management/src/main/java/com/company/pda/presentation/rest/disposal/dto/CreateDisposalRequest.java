package com.company.pda.presentation.rest.disposal.dto;

import com.company.pda.application.disposal.dto.CreateDisposalCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

public record CreateDisposalRequest(
    @NotNull UUID requestId,
    @NotBlank @Size(max = 1000) String remarks,
    @NotEmpty @Size(max = 100) List<@Valid DisposalLineRequest> items) {
  public CreateDisposalCommand toCommand() {
    return new CreateDisposalCommand(
        requestId, remarks, items.stream().map(DisposalLineRequest::toCommand).toList());
  }
}
