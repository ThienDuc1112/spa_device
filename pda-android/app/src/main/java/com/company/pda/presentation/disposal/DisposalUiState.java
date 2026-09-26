package com.company.pda.presentation.disposal;

public record DisposalUiState(
    java.util.List<com.company.pda.domain.model.Disposal> list,
    com.company.pda.domain.model.DisposalDetail detail,
    int page) {}
