package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.time.Instant;
import java.util.UUID;

public record RefreshEntity(
    UUID id,
    UUID familyId,
    long userId,
    String tokenHash,
    Instant expiresAt,
    Instant consumedAt,
    boolean revoked) {}
