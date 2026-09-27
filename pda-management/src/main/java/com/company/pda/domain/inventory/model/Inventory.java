package com.company.pda.domain.inventory.model;

import java.math.BigDecimal;

public record Inventory(long id, long productId, long storeId, BigDecimal quantity, long version) {}
