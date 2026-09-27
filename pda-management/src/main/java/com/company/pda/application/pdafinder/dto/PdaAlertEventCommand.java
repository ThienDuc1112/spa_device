package com.company.pda.application.pdafinder.dto;

import java.util.UUID;

public record PdaAlertEventCommand(UUID requestId, String status) {}
