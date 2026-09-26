package com.company.pda.domain.usecase;

import com.company.pda.domain.repository.DisposalRepository;

public final class ConfirmDisposalUseCase {
  private final DisposalRepository repository;

  public ConfirmDisposalUseCase(DisposalRepository repository) {
    this.repository = repository;
  }

  public com.company.pda.domain.model.Disposal execute(String id, long version)
      throws java.io.IOException {
    return repository.transition(id, version, true);
  }
}
