package com.company.pda.domain.inventory.model;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryAdjustment(
    UUID id,
    long productId,
    BigDecimal newQty,
    long expectedVersion,
    String reason,
    long createdBy) {}
