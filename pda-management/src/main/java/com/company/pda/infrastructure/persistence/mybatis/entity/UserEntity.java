package com.company.pda.infrastructure.persistence.mybatis.entity;

public record UserEntity(
    long id, String username, String passwordHash, long storeId, boolean active) {}
