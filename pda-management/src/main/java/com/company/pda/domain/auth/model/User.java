package com.company.pda.domain.auth.model;

public record User(long id, String username, String passwordHash, long storeId, boolean active) {}
