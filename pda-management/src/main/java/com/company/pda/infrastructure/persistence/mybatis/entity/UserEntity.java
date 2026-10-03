package com.company.pda.infrastructure.persistence.mybatis.entity;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class UserEntity {
  private final long id;
  private final String username;
  private final String passwordHash;
  private final long storeId;
  private final boolean active;

  @java.beans.ConstructorProperties({"id", "username", "passwordHash", "storeId", "active"})
  public UserEntity(long id, String username, String passwordHash, long storeId, boolean active) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.storeId = storeId;
    this.active = active;
  }

  public long id() {
    return id;
  }

  public long getId() {
    return id;
  }

  public String username() {
    return username;
  }

  public String getUsername() {
    return username;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public boolean active() {
    return active;
  }

  public boolean getActive() {
    return active;
  }
}
