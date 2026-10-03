package com.company.pda.presentation.rest.disposal.dto;

import com.company.pda.application.disposal.dto.DisposalTransitionCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DisposalTransitionRequest {
  private final @NotNull @PositiveOrZero Long version;

  @java.beans.ConstructorProperties({"version"})
  public DisposalTransitionRequest(Long version) {
    this.version = version;
  }

  public Long version() {
    return version;
  }

  public Long getVersion() {
    return version;
  }

  public DisposalTransitionCommand toCommand() {
    return new DisposalTransitionCommand(version);
  }
}
