package com.company.pda.presentation.disposal;

import com.company.pda.data.remote.dto.disposal.DisposalDto;

public record DisposalUiState(
    java.util.List<DisposalDto> list, DisposalDto.Detail detail, int page) {}
