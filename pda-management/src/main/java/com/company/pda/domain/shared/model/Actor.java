package com.company.pda.domain.shared.model;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class Actor {
  private final long id;
  private final long storeId;

  @java.beans.ConstructorProperties({"id", "storeId"})
  public Actor(long id, long storeId) {
    this.id = id;
    this.storeId = storeId;
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
}
