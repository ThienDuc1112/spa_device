package com.company.pda.domain.usecase;

import com.company.pda.domain.repository.PdaFinderRepository;

public final class StopPdaAlarmUseCase {
  private final PdaFinderRepository repository;

  public StopPdaAlarmUseCase(PdaFinderRepository repository) {
    this.repository = repository;
  }

  public void execute(String id) {
    repository.stopLocal(id);
  }
}
