package com.company.pda.domain.inventory.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryTransaction(
    long id,
    long storeId,
    long productId,
    String transactionType,
    BigDecimal qtyBefore,
    BigDecimal qtyChange,
    BigDecimal qtyAfter,
    UUID adjustmentId,
    UUID disposalId,
    long createdBy,
    Instant createdAt) {}
