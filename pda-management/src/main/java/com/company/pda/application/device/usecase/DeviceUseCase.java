package com.company.pda.application.device.usecase;

import com.company.pda.application.device.dto.DeviceResult;
import com.company.pda.domain.device.model.Device;

public interface DeviceUseCase extends RegisterDeviceUseCase, UpdateFcmTokenUseCase {
  Device authenticate(long id, String secret);

  java.util.List<DeviceResult> list();
}
