package com.company.pda.domain.disposal.model;

import java.math.BigDecimal;

public record DisposalItem(long productId, BigDecimal quantity, String reason) {}
