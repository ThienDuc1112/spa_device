package com.company.application;

import com.company.application.dto.Contracts.AlertEvent;
import com.company.domain.Models.FindRequest;
import java.util.UUID;

public interface FinderUseCase {
  FindRequest find(long deviceId);

  void stop(UUID id);

  void event(long deviceId, String secret, AlertEvent event);
}
