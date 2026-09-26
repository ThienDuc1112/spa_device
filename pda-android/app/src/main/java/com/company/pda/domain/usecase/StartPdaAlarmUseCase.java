package com.company.pda.domain.usecase;

import com.company.pda.domain.repository.PdaFinderRepository;

public final class StartPdaAlarmUseCase {
  private final PdaFinderRepository repository;

  public StartPdaAlarmUseCase(PdaFinderRepository repository) {
    this.repository = repository;
  }

  public void execute(String id, long expiresAtMillis) {
    repository.startLocal(id, expiresAtMillis);
  }
}
