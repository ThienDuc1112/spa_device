package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.time.Instant;
import java.util.UUID;

public record PdaFindRequestEntity(
    UUID id, long requesterId, long storeId, long deviceId, String status, Instant expiresAt) {}
