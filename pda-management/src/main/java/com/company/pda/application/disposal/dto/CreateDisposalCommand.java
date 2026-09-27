package com.company.pda.application.disposal.dto;

import java.util.List;
import java.util.UUID;

public record CreateDisposalCommand(
    UUID requestId, String remarks, List<DisposalLineCommand> items) {}
