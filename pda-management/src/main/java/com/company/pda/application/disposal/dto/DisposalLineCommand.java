package com.company.pda.application.disposal.dto;

import java.math.BigDecimal;

public record DisposalLineCommand(String productCode, BigDecimal quantity, String reason) {}
