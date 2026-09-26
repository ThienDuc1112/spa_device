package com.company.application;

import com.company.application.dto.Contracts.*;
import com.company.domain.Models.Device;

public interface DeviceUseCase {
  Registration register(Register body);

  Device authenticate(long id, String secret);

  void token(long id, String secret, Token body);
}
