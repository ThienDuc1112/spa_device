package com.company.pda.application.pdafinder.service;

import static com.company.pda.domain.shared.exception.DomainException.*;

import com.company.pda.application.device.usecase.DeviceUseCase;
import com.company.pda.application.pdafinder.dto.FindPdaCommand;
import com.company.pda.application.pdafinder.dto.FindPdaResult;
import com.company.pda.application.pdafinder.dto.PdaAlertEventCommand;
import com.company.pda.application.pdafinder.usecase.FinderUseCase;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.application.port.out.OperationsRepository;
import com.company.pda.domain.device.repository.DeviceRepository;
import com.company.pda.domain.pdafinder.exception.DeviceNotInStoreException;
import com.company.pda.domain.pdafinder.model.PdaFindStatus;
import com.company.pda.domain.pdafinder.repository.PdaFindRepository;
import com.company.pda.domain.shared.exception.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PdaFinderService implements FinderUseCase {
  private final PdaFindRepository repo;
  private final DeviceRepository devices;
  private final DeviceUseCase identity;
  private final OperationsRepository ops;
  private final CurrentActor actor;
  private final int timeout;

  public PdaFinderService(
      PdaFindRepository repo,
      DeviceRepository devices,
      DeviceUseCase identity,
      OperationsRepository ops,
      CurrentActor actor,
      @Value("${app.finder-timeout-seconds}") int timeout) {
    this.repo = repo;
    this.devices = devices;
    this.identity = identity;
    this.ops = ops;
    this.actor = actor;
    this.timeout = Math.max(10, Math.min(300, timeout));
  }

  @Transactional
  public FindPdaResult find(FindPdaCommand command) {
    var a = actor.get();
    long deviceId = command.deviceId();
    var d = devices.find(deviceId, a.storeId());
    if (d == null) throw new DeviceNotInStoreException();
    require(d.fcmToken() != null, "Device has no valid push token; register it again");
    UUID id = UUID.randomUUID();
    repo.create(id, a.id(), a.storeId(), deviceId, Instant.now().plusSeconds(timeout));
    repo.log(id, deviceId, PdaFindStatus.QUEUED.name(), null);
    ops.enqueue("FIND", id.toString(), "{\"storeId\":" + a.storeId() + "}");
    ops.audit(a.id(), a.storeId(), "PDA_FIND", id.toString());
    return FindPdaResult.from(found(repo.find(id, a.storeId())));
  }

  public FindPdaResult status(UUID id) {
    return FindPdaResult.from(found(repo.find(id, actor.get().storeId())));
  }

  @Transactional
  public void stop(UUID id) {
    var a = actor.get();
    var r = found(repo.find(id, a.storeId()));
    if (repo.status(id, PdaFindStatus.STOPPED.name()) == 1) {
      repo.log(id, r.deviceId(), "STOP_REQUESTED", null);
      ops.enqueue("STOP", id.toString(), "{\"storeId\":" + a.storeId() + "}");
      ops.audit(a.id(), a.storeId(), "PDA_STOP", id.toString());
    }
  }

  @Transactional
  public void event(long deviceId, String secret, PdaAlertEventCommand body) {
    var d = identity.authenticate(deviceId, secret);
    var r = found(repo.find(body.requestId(), d.storeId()));
    if (r.deviceId() != deviceId) throw new DomainException(403, "Wrong target device");
    String status = body.status();
    if (status.equals(PdaFindStatus.RINGING.name()) && !r.expiresAt().isAfter(Instant.now()))
      status = PdaFindStatus.EXPIRED.name();
    if (repo.status(r.id(), status) == 1) repo.log(r.id(), deviceId, status, null);
  }
}
