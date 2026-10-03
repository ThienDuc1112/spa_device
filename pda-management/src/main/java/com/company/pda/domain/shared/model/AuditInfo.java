package com.company.pda.domain.shared.model;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class AuditInfo {
  private final Long actorId;
  private final long storeId;
  private final String operation;
  private final String reference;

  @java.beans.ConstructorProperties({"actorId", "storeId", "operation", "reference"})
  public AuditInfo(Long actorId, long storeId, String operation, String reference) {
    this.actorId = actorId;
    this.storeId = storeId;
    this.operation = operation;
    this.reference = reference;
  }

  public Long actorId() {
    return actorId;
  }

  public Long getActorId() {
    return actorId;
  }

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public String operation() {
    return operation;
  }

  public String getOperation() {
    return operation;
  }

  public String reference() {
    return reference;
  }

  public String getReference() {
    return reference;
  }
}
