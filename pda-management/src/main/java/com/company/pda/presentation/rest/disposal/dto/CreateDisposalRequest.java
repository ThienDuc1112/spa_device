package com.company.pda.presentation.rest.disposal.dto;

import com.company.pda.application.disposal.dto.CreateDisposalCommand;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class CreateDisposalRequest {
  private final @NotNull UUID requestId;
  private final @NotBlank @Size(max = 1000) String remarks;
  private final @NotEmpty @Size(max = 100) List<@Valid DisposalLineRequest> items;

  @java.beans.ConstructorProperties({"requestId", "remarks", "items"})
  public CreateDisposalRequest(UUID requestId, String remarks, List<DisposalLineRequest> items) {
    this.requestId = requestId;
    this.remarks = remarks;
    this.items = items;
  }

  public UUID requestId() {
    return requestId;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public String remarks() {
    return remarks;
  }

  public String getRemarks() {
    return remarks;
  }

  public List<DisposalLineRequest> items() {
    return items;
  }

  public List<DisposalLineRequest> getItems() {
    return items;
  }

  public CreateDisposalCommand toCommand() {
    return new CreateDisposalCommand(
        requestId,
        remarks,
        items.stream()
            .map(DisposalLineRequest::toCommand)
            .collect(java.util.stream.Collectors.toList()));
  }
}
