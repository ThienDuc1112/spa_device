package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.time.Instant;

public record DeviceEntity(
    long id,
    String deviceCode,
    String deviceName,
    long storeId,
    String credentialHash,
    String fcmToken,
    Instant lastActiveAt) {}
