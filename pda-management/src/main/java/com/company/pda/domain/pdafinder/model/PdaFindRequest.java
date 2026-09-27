package com.company.pda.domain.pdafinder.model;

import java.time.Instant;
import java.util.UUID;

public record PdaFindRequest(
    UUID id, long requesterId, long storeId, long deviceId, String status, Instant expiresAt) {}
