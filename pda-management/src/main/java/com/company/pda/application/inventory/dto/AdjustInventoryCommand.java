package com.company.pda.application.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustInventoryCommand(
    UUID requestId, String productCode, BigDecimal quantity, Long version, String reason) {}
