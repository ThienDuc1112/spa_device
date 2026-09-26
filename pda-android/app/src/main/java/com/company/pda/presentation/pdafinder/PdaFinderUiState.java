package com.company.pda.presentation.pdafinder;

public record PdaFinderUiState(
    java.util.List<com.company.pda.domain.model.Device> devices,
    com.company.pda.domain.model.PdaFindRequest request) {}
