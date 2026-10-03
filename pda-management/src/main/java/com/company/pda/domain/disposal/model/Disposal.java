package com.company.pda.domain.disposal.model;

import java.time.Instant;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class Disposal {
  private final UUID id;
  private final long storeId;
  private final String status;
  private final String remarks;
  private final long createdBy;
  private final long version;
  private final Instant createdAt;

  @java.beans.ConstructorProperties({
    "id",
    "storeId",
    "status",
    "remarks",
    "createdBy",
    "version",
    "createdAt"
  })
  public Disposal(
      UUID id,
      long storeId,
      String status,
      String remarks,
      long createdBy,
      long version,
      Instant createdAt) {
    this.id = id;
    this.storeId = storeId;
    this.status = status;
    this.remarks = remarks;
    this.createdBy = createdBy;
    this.version = version;
    this.createdAt = createdAt;
  }

  public UUID id() {
    return id;
  }

  public UUID getId() {
    return id;
  }

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public String status() {
    return status;
  }

  public String getStatus() {
    return status;
  }

  public String remarks() {
    return remarks;
  }

  public String getRemarks() {
    return remarks;
  }

  public long createdBy() {
    return createdBy;
  }

  public long getCreatedBy() {
    return createdBy;
  }

  public long version() {
    return version;
  }

  public long getVersion() {
    return version;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
