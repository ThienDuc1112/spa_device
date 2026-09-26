package com.company.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Models {
  private Models() {}

  public record User(long id, String username, String passwordHash, long storeId, boolean active) {}

  public record Device(
      long id,
      String deviceCode,
      String deviceName,
      long storeId,
      String credentialHash,
      String fcmToken,
      Instant lastActiveAt) {}

  public record Product(
      long id,
      String barcode,
      String productCode,
      String productName,
      String imageUrl,
      long imageVersion) {}

  public record Inventory(
      long id, long productId, long storeId, BigDecimal quantity, long version) {}

  public record Disposal(
      UUID id,
      long storeId,
      String status,
      String remarks,
      long createdBy,
      long version,
      Instant createdAt) {}

  public record DisposalItem(long productId, BigDecimal quantity, String reason) {}

  public record FindRequest(
      UUID id, long requesterId, long storeId, long deviceId, String status, Instant expiresAt) {}

  public record Refresh(
      UUID id,
      UUID familyId,
      long userId,
      String tokenHash,
      Instant expiresAt,
      Instant consumedAt,
      boolean revoked) {}

  public record Outbox(
      long id, String eventType, String aggregateId, String payload, int attempts) {}

  public record Actor(long id, long storeId) {}
}
