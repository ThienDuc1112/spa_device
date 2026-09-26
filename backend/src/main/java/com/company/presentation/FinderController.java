package com.company.presentation;

import com.company.application.*;
import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pda")
public class FinderController {
  private final FinderUseCase service;
  private final FinderRepository repo;
  private final CurrentActor actor;

  public FinderController(FinderUseCase service, FinderRepository repo, CurrentActor actor) {
    this.service = service;
    this.repo = repo;
    this.actor = actor;
  }

  @PostMapping("/find")
  @PreAuthorize("hasRole('MANAGER')")
  public Object find(@Valid @RequestBody Find b) {
    return service.find(b.deviceId());
  }

  @GetMapping("/find/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  public Object status(@PathVariable UUID id) {
    return BusinessException.found(repo.find(id, actor.get().storeId()));
  }

  @PostMapping("/stop")
  @PreAuthorize("hasRole('MANAGER')")
  public void stop(@Valid @RequestBody Stop b) {
    service.stop(b.requestId());
  }

  @PostMapping("/events")
  public void event(
      @RequestHeader("X-Device-Id") long id,
      @RequestHeader("X-Device-Secret") String secret,
      @Valid @RequestBody AlertEvent b) {
    service.event(id, secret, b);
  }
}
