package com.company.pda.domain.disposal.model;

import java.time.Instant;
import java.util.UUID;

public record Disposal(
    UUID id,
    long storeId,
    String status,
    String remarks,
    long createdBy,
    long version,
    Instant createdAt) {}
