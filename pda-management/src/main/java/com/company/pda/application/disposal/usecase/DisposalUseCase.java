package com.company.pda.application.disposal.usecase;

import com.company.pda.domain.disposal.model.Disposal;

public interface DisposalUseCase
    extends CreateDisposalUseCase, ConfirmDisposalUseCase, CancelDisposalUseCase {
  Object detail(java.util.UUID id);

  java.util.List<Disposal> list(int page);
}
