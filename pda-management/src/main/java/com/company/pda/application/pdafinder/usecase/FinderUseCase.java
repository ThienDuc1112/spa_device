package com.company.pda.application.pdafinder.usecase;

import com.company.pda.application.pdafinder.dto.FindPdaResult;
import com.company.pda.application.pdafinder.dto.PdaAlertEventCommand;

public interface FinderUseCase extends FindPdaUseCase, StopPdaAlertUseCase {
  com.company.pda.application.pdafinder.dto.FcmHealthResult fcmHealth(long deviceId, String secret);

  java.util.List<com.company.pda.application.pdafinder.dto.DeviceFinderCommand> commands(
      long deviceId, String secret);

  void event(long deviceId, String secret, PdaAlertEventCommand event);

  FindPdaResult status(java.util.UUID id);
}
