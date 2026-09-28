package com.company.pda.application.pdafinder.dto;

import java.time.Instant;
import java.util.UUID;

public record DeviceFinderCommand(UUID requestId, String command, Instant expiresAt) {}
