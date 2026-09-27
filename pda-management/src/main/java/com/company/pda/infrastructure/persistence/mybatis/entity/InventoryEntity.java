package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.math.BigDecimal;

public record InventoryEntity(
    long id, long productId, long storeId, BigDecimal quantity, long version) {}
