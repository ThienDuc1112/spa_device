package com.company.pda.application.disposal.dto;

import java.util.List;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class CreateDisposalCommand {
  private final UUID requestId;
  private final String remarks;
  private final List<DisposalLineCommand> items;

  @java.beans.ConstructorProperties({"requestId", "remarks", "items"})
  public CreateDisposalCommand(UUID requestId, String remarks, List<DisposalLineCommand> items) {
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

  public List<DisposalLineCommand> items() {
    return items;
  }

  public List<DisposalLineCommand> getItems() {
    return items;
  }
}
