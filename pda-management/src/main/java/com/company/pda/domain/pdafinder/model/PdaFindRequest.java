package com.company.pda.domain.pdafinder.model;

import java.time.Instant;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class PdaFindRequest {
  private final UUID id;
  private final Long requesterId;
  private final long storeId;
  private final long deviceId;
  private final String status;
  private final Instant expiresAt;

  @java.beans.ConstructorProperties({
    "id",
    "requesterId",
    "storeId",
    "deviceId",
    "status",
    "expiresAt"
  })
  public PdaFindRequest(
      UUID id, Long requesterId, long storeId, long deviceId, String status, Instant expiresAt) {
    this.id = id;
    this.requesterId = requesterId;
    this.storeId = storeId;
    this.deviceId = deviceId;
    this.status = status;
    this.expiresAt = expiresAt;
  }

  public UUID id() {
    return id;
  }

  public UUID getId() {
    return id;
  }

  public Long requesterId() {
    return requesterId;
  }

  public Long getRequesterId() {
    return requesterId;
  }

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public long deviceId() {
    return deviceId;
  }

  public long getDeviceId() {
    return deviceId;
  }

  public String status() {
    return status;
  }

  public String getStatus() {
    return status;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
