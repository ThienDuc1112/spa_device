package com.company.pda.domain.pdafinder.model;

import java.util.UUID;

public record PdaAlertLog(UUID requestId, long deviceId, String event, String message) {}
