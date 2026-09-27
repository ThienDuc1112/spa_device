package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.time.Instant;
import java.util.UUID;

public record DisposalEntity(
    UUID id,
    long storeId,
    String status,
    String remarks,
    long createdBy,
    long version,
    Instant createdAt) {}
