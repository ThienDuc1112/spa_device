package com.company.pda.application.device.usecase;

import com.company.pda.application.device.dto.UpdateFcmTokenCommand;

public interface UpdateFcmTokenUseCase {
  void token(long id, String secret, UpdateFcmTokenCommand body);
}
