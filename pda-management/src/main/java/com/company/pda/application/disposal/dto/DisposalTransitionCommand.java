package com.company.pda.application.disposal.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DisposalTransitionCommand {
  private final Long version;

  @java.beans.ConstructorProperties({"version"})
  public DisposalTransitionCommand(Long version) {
    this.version = version;
  }

  public Long version() {
    return version;
  }

  public Long getVersion() {
    return version;
  }
}
