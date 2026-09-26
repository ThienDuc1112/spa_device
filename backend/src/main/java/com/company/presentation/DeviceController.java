package com.company.presentation;

import com.company.application.*;
import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
public class DeviceController {
  private final DeviceUseCase service;
  private final DeviceRepository repo;
  private final CurrentActor actor;

  public DeviceController(DeviceUseCase service, DeviceRepository repo, CurrentActor actor) {
    this.service = service;
    this.repo = repo;
    this.actor = actor;
  }

  @PostMapping("/register")
  @PreAuthorize("hasRole('MANAGER')")
  public Registration register(@Valid @RequestBody Register body) {
    return service.register(body);
  }

  @GetMapping
  @PreAuthorize("hasRole('MANAGER')")
  public Object list() {
    return repo.list(actor.get().storeId()).stream()
        .map(
            d ->
                java.util.Map.of(
                    "id",
                    d.id(),
                    "deviceCode",
                    d.deviceCode(),
                    "deviceName",
                    d.deviceName(),
                    "reachable",
                    d.fcmToken() != null,
                    "lastActiveAt",
                    d.lastActiveAt() == null ? "" : d.lastActiveAt().toString()))
        .toList();
  }

  @PutMapping("/token")
  public void token(
      @RequestHeader("X-Device-Id") long id,
      @RequestHeader("X-Device-Secret") String secret,
      @Valid @RequestBody Token body) {
    service.token(id, secret, body);
  }
}
