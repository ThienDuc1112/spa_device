package com.company.pda.domain.inventory.model;

import java.math.BigDecimal;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class InventoryAdjustment {
  private final UUID id;
  private final long productId;
  private final BigDecimal newQty;
  private final long expectedVersion;
  private final String reason;
  private final long createdBy;

  @java.beans.ConstructorProperties({
    "id",
    "productId",
    "newQty",
    "expectedVersion",
    "reason",
    "createdBy"
  })
  public InventoryAdjustment(
      UUID id,
      long productId,
      BigDecimal newQty,
      long expectedVersion,
      String reason,
      long createdBy) {
    this.id = id;
    this.productId = productId;
    this.newQty = newQty;
    this.expectedVersion = expectedVersion;
    this.reason = reason;
    this.createdBy = createdBy;
  }

  public UUID id() {
    return id;
  }

  public UUID getId() {
    return id;
  }

  public long productId() {
    return productId;
  }

  public long getProductId() {
    return productId;
  }

  public BigDecimal newQty() {
    return newQty;
  }

  public BigDecimal getNewQty() {
    return newQty;
  }

  public long expectedVersion() {
    return expectedVersion;
  }

  public long getExpectedVersion() {
    return expectedVersion;
  }

  public String reason() {
    return reason;
  }

  public String getReason() {
    return reason;
  }

  public long createdBy() {
    return createdBy;
  }

  public long getCreatedBy() {
    return createdBy;
  }
}
