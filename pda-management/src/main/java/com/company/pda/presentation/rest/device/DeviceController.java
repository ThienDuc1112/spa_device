package com.company.pda.presentation.rest.device;

import com.company.pda.application.device.dto.DeviceRegistrationResult;
import com.company.pda.application.device.usecase.DeviceUseCase;
import com.company.pda.presentation.rest.device.dto.RegisterDeviceRequest;
import com.company.pda.presentation.rest.device.dto.UpdateFcmTokenRequest;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
public class DeviceController {
  private final DeviceUseCase service;

  public DeviceController(DeviceUseCase service) {
    this.service = service;
  }

  @PostMapping("/register")
  @PreAuthorize("hasRole('MANAGER')")
  public DeviceRegistrationResult register(@Valid @RequestBody RegisterDeviceRequest body) {
    return service.register(body.toCommand());
  }

  @GetMapping
  @PreAuthorize("hasRole('MANAGER')")
  public Object list() {
    return service.list();
  }

  @PutMapping("/token")
  public void token(
      @RequestHeader("X-Device-Id") long id,
      @RequestHeader("X-Device-Secret") String secret,
      @Valid @RequestBody UpdateFcmTokenRequest body) {
    service.token(id, secret, body.toCommand());
  }
}
