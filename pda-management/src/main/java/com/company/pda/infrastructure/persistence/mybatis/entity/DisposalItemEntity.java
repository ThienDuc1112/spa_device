package com.company.pda.infrastructure.persistence.mybatis.entity;

import java.math.BigDecimal;

public record DisposalItemEntity(long productId, BigDecimal quantity, String reason) {}
