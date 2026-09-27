package com.company.pda.domain.shared.model;

public record AuditInfo(Long actorId, long storeId, String operation, String reference) {}
