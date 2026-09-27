package com.company.pda.application.pdafinder.usecase;

import com.company.pda.application.pdafinder.dto.FindPdaResult;
import com.company.pda.application.pdafinder.dto.PdaAlertEventCommand;

public interface FinderUseCase extends FindPdaUseCase, StopPdaAlertUseCase {
  void event(long deviceId, String secret, PdaAlertEventCommand event);

  FindPdaResult status(java.util.UUID id);
}
