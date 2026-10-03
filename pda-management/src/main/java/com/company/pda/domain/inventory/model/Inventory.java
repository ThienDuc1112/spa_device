package com.company.pda.domain.inventory.model;

import java.math.BigDecimal;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class Inventory {
  private final long id;
  private final long productId;
  private final long storeId;
  private final BigDecimal quantity;
  private final long version;

  @java.beans.ConstructorProperties({"id", "productId", "storeId", "quantity", "version"})
  public Inventory(long id, long productId, long storeId, BigDecimal quantity, long version) {
    this.id = id;
    this.productId = productId;
    this.storeId = storeId;
    this.quantity = quantity;
    this.version = version;
  }

  public long id() {
    return id;
  }

  public long getId() {
    return id;
  }

  public long productId() {
    return productId;
  }

  public long getProductId() {
    return productId;
  }

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public BigDecimal quantity() {
    return quantity;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public long version() {
    return version;
  }

  public long getVersion() {
    return version;
  }
}
