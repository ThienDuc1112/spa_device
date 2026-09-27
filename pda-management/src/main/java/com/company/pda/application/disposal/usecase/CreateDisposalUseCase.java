package com.company.pda.application.disposal.usecase;

import com.company.pda.application.disposal.dto.CreateDisposalCommand;
import com.company.pda.domain.disposal.model.Disposal;

public interface CreateDisposalUseCase {
  Disposal create(CreateDisposalCommand body);
}
