package com.company.pda.application.disposal.usecase;

import com.company.pda.domain.disposal.model.Disposal;

public interface ConfirmDisposalUseCase {
  Disposal confirm(java.util.UUID id, long version);
}
