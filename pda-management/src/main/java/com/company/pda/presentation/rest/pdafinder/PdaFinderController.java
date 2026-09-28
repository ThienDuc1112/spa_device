package com.company.pda.presentation.rest.pdafinder;

import com.company.pda.application.pdafinder.usecase.FinderUseCase;
import com.company.pda.presentation.rest.pdafinder.dto.FindPdaRequest;
import com.company.pda.presentation.rest.pdafinder.dto.FindPdaResponse;
import com.company.pda.presentation.rest.pdafinder.dto.PdaAlertEventRequest;
import com.company.pda.presentation.rest.pdafinder.dto.StopPdaRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pda")
public class PdaFinderController {
  private final FinderUseCase service;

  public PdaFinderController(FinderUseCase service) {
    this.service = service;
  }

  @PostMapping("/find")
  @PreAuthorize("hasRole('MANAGER')")
  public FindPdaResponse find(@Valid @RequestBody FindPdaRequest b) {
    return FindPdaResponse.from(service.find(b.toCommand()));
  }

  @GetMapping("/find/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  public FindPdaResponse status(@PathVariable UUID id) {
    return FindPdaResponse.from(service.status(id));
  }

  @PostMapping("/stop")
  @PreAuthorize("hasRole('MANAGER')")
  public void stop(@Valid @RequestBody StopPdaRequest b) {
    service.stop(b.requestId());
  }

  @PostMapping("/events")
  public void event(
      @RequestHeader("X-Device-Id") long id,
      @RequestHeader("X-Device-Secret") String secret,
      @Valid @RequestBody PdaAlertEventRequest b) {
    service.event(id, secret, b.toCommand());
  }

  @GetMapping("/commands")
  public org.springframework.http.ResponseEntity<
          java.util.List<com.company.pda.application.pdafinder.dto.DeviceFinderCommand>>
      commands(
          @RequestHeader("X-Device-Id") long id, @RequestHeader("X-Device-Secret") String secret) {
    return org.springframework.http.ResponseEntity.ok()
        .cacheControl(org.springframework.http.CacheControl.noStore())
        .body(service.commands(id, secret));
  }

  @GetMapping("/fcm-health")
  public org.springframework.http.ResponseEntity<
          com.company.pda.application.pdafinder.dto.FcmHealthResult>
      fcmHealth(
          @RequestHeader("X-Device-Id") long id, @RequestHeader("X-Device-Secret") String secret) {
    return org.springframework.http.ResponseEntity.ok()
        .cacheControl(org.springframework.http.CacheControl.noStore())
        .body(service.fcmHealth(id, secret));
  }
}
