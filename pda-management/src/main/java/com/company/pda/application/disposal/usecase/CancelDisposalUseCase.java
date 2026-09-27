package com.company.pda.application.disposal.usecase;

import com.company.pda.domain.disposal.model.Disposal;

public interface CancelDisposalUseCase {
  Disposal cancel(java.util.UUID id, long version);
}
