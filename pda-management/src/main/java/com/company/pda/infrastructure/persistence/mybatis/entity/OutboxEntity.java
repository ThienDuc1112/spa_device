package com.company.pda.infrastructure.persistence.mybatis.entity;

public record OutboxEntity(
    long id, String eventType, String aggregateId, String payload, int attempts) {}
