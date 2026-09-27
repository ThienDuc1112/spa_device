package com.company.pda.domain.shared.model;

public record Outbox(long id, String eventType, String aggregateId, String payload, int attempts) {}
