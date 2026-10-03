package com.company.pda.application.auth.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RegisterUserResult {
  private final long id;
  private final String username;
  private final long storeId;
  private final String role;

  @java.beans.ConstructorProperties({"id", "username", "storeId", "role"})
  public RegisterUserResult(long id, String username, long storeId, String role) {
    this.id = id;
    this.username = username;
    this.storeId = storeId;
    this.role = role;
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

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public String role() {
    return role;
  }

  public String getRole() {
    return role;
  }
}
