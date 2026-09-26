package com.company.presentation;

import com.company.application.*;
import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disposals")
public class DisposalController {
  private final DisposalUseCase service;
  private final DisposalRepository repo;
  private final CurrentActor actor;

  public DisposalController(DisposalUseCase service, DisposalRepository repo, CurrentActor actor) {
    this.service = service;
    this.repo = repo;
    this.actor = actor;
  }

  @GetMapping
  public Object list(@RequestParam(defaultValue = "0") int page) {
    if (page < 0 || page > 10000) throw new BusinessException(400, "Invalid page");
    return repo.list(actor.get().storeId(), page * 50);
  }

  @GetMapping("/{id}")
  public Object detail(@PathVariable UUID id) {
    return service.detail(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
  public Object create(@Valid @RequestBody DisposalCreate body) {
    return service.create(body);
  }

  @PostMapping("/{id}/confirm")
  @PreAuthorize("hasRole('MANAGER')")
  public Object confirm(@PathVariable UUID id, @Valid @RequestBody Transition body) {
    return service.transition(id, body.version(), true);
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasRole('MANAGER')")
  public Object cancel(@PathVariable UUID id, @Valid @RequestBody Transition body) {
    return service.transition(id, body.version(), false);
  }
}
