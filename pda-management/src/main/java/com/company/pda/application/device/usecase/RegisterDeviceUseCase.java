package com.company.pda.application.device.usecase;

import com.company.pda.application.device.dto.DeviceRegistrationResult;
import com.company.pda.application.device.dto.RegisterDeviceCommand;

public interface RegisterDeviceUseCase {
  DeviceRegistrationResult register(RegisterDeviceCommand body);
}
