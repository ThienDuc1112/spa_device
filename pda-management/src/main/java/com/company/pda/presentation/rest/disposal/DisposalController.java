package com.company.pda.presentation.rest.disposal;

import com.company.pda.application.disposal.usecase.DisposalUseCase;
import com.company.pda.presentation.rest.disposal.dto.CreateDisposalRequest;
import com.company.pda.presentation.rest.disposal.dto.DisposalTransitionRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disposals")
public class DisposalController {
  private final DisposalUseCase service;

  public DisposalController(DisposalUseCase service) {
    this.service = service;
  }

  @GetMapping
  public Object list(@RequestParam(defaultValue = "0") int page) {
    return service.list(page);
  }

  @GetMapping("/{id}")
  public Object detail(@PathVariable UUID id) {
    return service.detail(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
  public Object create(@Valid @RequestBody CreateDisposalRequest body) {
    return service.create(body.toCommand());
  }

  @PostMapping("/{id}/confirm")
  @PreAuthorize("hasRole('MANAGER')")
  public Object confirm(@PathVariable UUID id, @Valid @RequestBody DisposalTransitionRequest body) {
    return service.confirm(id, body.version());
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasRole('MANAGER')")
  public Object cancel(@PathVariable UUID id, @Valid @RequestBody DisposalTransitionRequest body) {
    return service.cancel(id, body.version());
  }
}
