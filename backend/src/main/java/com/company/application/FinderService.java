package com.company.application;

import static com.company.domain.BusinessException.*;

import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import com.company.domain.Models.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinderService implements FinderUseCase {
  private final FinderRepository repo;
  private final DeviceRepository devices;
  private final DeviceUseCase identity;
  private final OperationsRepository ops;
  private final CurrentActor actor;
  private final int timeout;

  public FinderService(
      FinderRepository repo,
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
  public FindRequest find(long deviceId) {
    var a = actor.get();
    var d = found(devices.find(deviceId, a.storeId()));
    require(d.fcmToken() != null, "Device has no valid push token; register it again");
    UUID id = UUID.randomUUID();
    repo.create(id, a.id(), a.storeId(), deviceId, Instant.now().plusSeconds(timeout));
    repo.log(id, deviceId, "QUEUED", null);
    ops.enqueue("FIND", id.toString(), "{\"storeId\":" + a.storeId() + "}");
    ops.audit(a.id(), a.storeId(), "PDA_FIND", id.toString());
    return found(repo.find(id, a.storeId()));
  }

  @Transactional
  public void stop(UUID id) {
    var a = actor.get();
    var r = found(repo.find(id, a.storeId()));
    if (repo.status(id, "STOPPED") == 1) {
      repo.log(id, r.deviceId(), "STOP_REQUESTED", null);
      ops.enqueue("STOP", id.toString(), "{\"storeId\":" + a.storeId() + "}");
      ops.audit(a.id(), a.storeId(), "PDA_STOP", id.toString());
    }
  }

  @Transactional
  public void event(long deviceId, String secret, AlertEvent body) {
    var d = identity.authenticate(deviceId, secret);
    var r = found(repo.find(body.requestId(), d.storeId()));
    if (r.deviceId() != deviceId) throw new BusinessException(403, "Wrong target device");
    String status = body.status();
    if (status.equals("RINGING") && !r.expiresAt().isAfter(Instant.now())) status = "EXPIRED";
    if (repo.status(r.id(), status) == 1) repo.log(r.id(), deviceId, status, null);
  }
}
