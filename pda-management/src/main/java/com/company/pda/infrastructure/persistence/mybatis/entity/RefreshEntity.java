package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.time.Instant;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RefreshEntity {
  private final UUID id;
  private final UUID familyId;
  private final long userId;
  private final String tokenHash;
  private final Instant expiresAt;
  private final Instant consumedAt;
  private final boolean revoked;

  @java.beans.ConstructorProperties({
    "id",
    "familyId",
    "userId",
    "tokenHash",
    "expiresAt",
    "consumedAt",
    "revoked"
  })
  public RefreshEntity(
      UUID id,
      UUID familyId,
      long userId,
      String tokenHash,
      Instant expiresAt,
      Instant consumedAt,
      boolean revoked) {
    this.id = id;
    this.familyId = familyId;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.consumedAt = consumedAt;
    this.revoked = revoked;
  }

  public UUID id() {
    return id;
  }

  public UUID getId() {
    return id;
  }

  public UUID familyId() {
    return familyId;
  }

  public UUID getFamilyId() {
    return familyId;
  }

  public long userId() {
    return userId;
  }

  public long getUserId() {
    return userId;
  }

  public String tokenHash() {
    return tokenHash;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant consumedAt() {
    return consumedAt;
  }

  public Instant getConsumedAt() {
    return consumedAt;
  }

  public boolean revoked() {
    return revoked;
  }

  public boolean getRevoked() {
    return revoked;
  }
}
