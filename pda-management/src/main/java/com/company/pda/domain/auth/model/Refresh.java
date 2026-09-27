package com.company.pda.domain.auth.model;

import java.time.Instant;
import java.util.UUID;

public record Refresh(
    UUID id,
    UUID familyId,
    long userId,
    String tokenHash,
    Instant expiresAt,
    Instant consumedAt,
    boolean revoked) {}
