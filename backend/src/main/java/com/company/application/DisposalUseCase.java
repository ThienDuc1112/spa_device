package com.company.application;

import com.company.application.dto.Contracts.*;
import com.company.domain.Models.Disposal;
import java.util.UUID;

public interface DisposalUseCase {
  Object detail(UUID id);

  Disposal create(DisposalCreate body);

  Disposal transition(UUID id, long version, boolean confirm);
}
