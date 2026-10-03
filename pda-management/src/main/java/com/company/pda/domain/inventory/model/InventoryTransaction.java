package com.company.pda.domain.inventory.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class InventoryTransaction {
  private final long id;
  private final long storeId;
  private final long productId;
  private final String transactionType;
  private final BigDecimal qtyBefore;
  private final BigDecimal qtyChange;
  private final BigDecimal qtyAfter;
  private final UUID adjustmentId;
  private final UUID disposalId;
  private final long createdBy;
  private final Instant createdAt;

  @java.beans.ConstructorProperties({
    "id",
    "storeId",
    "productId",
    "transactionType",
    "qtyBefore",
    "qtyChange",
    "qtyAfter",
    "adjustmentId",
    "disposalId",
    "createdBy",
    "createdAt"
  })
  public InventoryTransaction(
      long id,
      long storeId,
      long productId,
      String transactionType,
      BigDecimal qtyBefore,
      BigDecimal qtyChange,
      BigDecimal qtyAfter,
      UUID adjustmentId,
      UUID disposalId,
      long createdBy,
      Instant createdAt) {
    this.id = id;
    this.storeId = storeId;
    this.productId = productId;
    this.transactionType = transactionType;
    this.qtyBefore = qtyBefore;
    this.qtyChange = qtyChange;
    this.qtyAfter = qtyAfter;
    this.adjustmentId = adjustmentId;
    this.disposalId = disposalId;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
  }

  public long id() {
    return id;
  }

  public long getId() {
    return id;
  }

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public long productId() {
    return productId;
  }

  public long getProductId() {
    return productId;
  }

  public String transactionType() {
    return transactionType;
  }

  public String getTransactionType() {
    return transactionType;
  }

  public BigDecimal qtyBefore() {
    return qtyBefore;
  }

  public BigDecimal getQtyBefore() {
    return qtyBefore;
  }

  public BigDecimal qtyChange() {
    return qtyChange;
  }

  public BigDecimal getQtyChange() {
    return qtyChange;
  }

  public BigDecimal qtyAfter() {
    return qtyAfter;
  }

  public BigDecimal getQtyAfter() {
    return qtyAfter;
  }

  public UUID adjustmentId() {
    return adjustmentId;
  }

  public UUID getAdjustmentId() {
    return adjustmentId;
  }

  public UUID disposalId() {
    return disposalId;
  }

  public UUID getDisposalId() {
    return disposalId;
  }

  public long createdBy() {
    return createdBy;
  }

  public long getCreatedBy() {
    return createdBy;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
