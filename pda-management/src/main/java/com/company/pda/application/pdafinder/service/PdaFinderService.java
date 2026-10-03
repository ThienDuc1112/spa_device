package com.company.pda.application.pdafinder.service;

import static com.company.pda.domain.shared.exception.DomainException.*;

import com.company.pda.application.device.usecase.DeviceUseCase;
import com.company.pda.application.pdafinder.dto.FcmHealthResult;
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
  private final boolean fcmEnabled;

  public PdaFinderService(
      PdaFindRepository repo,
      DeviceRepository devices,
      DeviceUseCase identity,
      OperationsRepository ops,
      CurrentActor actor,
      @Value("${app.finder-timeout-seconds}") int timeout,
      @Value("${app.fcm-enabled:false}") boolean fcmEnabled) {
    this.repo = repo;
    this.devices = devices;
    this.identity = identity;
    this.ops = ops;
    this.actor = actor;
    this.timeout = Math.max(10, Math.min(300, timeout));
    this.fcmEnabled = fcmEnabled;
  }

  @Transactional
  public FindPdaResult find(FindPdaCommand command) {
    lombok.val a = actor.get();
    return findForStore(command.deviceId(), a.storeId(), a.id());
  }

  @Transactional
  public FindPdaResult findFromWeb(long deviceId) {
    return findForStore(deviceId, found(devices.identity(deviceId)).storeId(), null);
  }

  private FindPdaResult findForStore(long deviceId, long storeId, Long requesterId) {
    lombok.val d = devices.find(deviceId, storeId);
    if (d == null) throw new DeviceNotInStoreException();
    // A disabled or delayed scheduler must not leave this device blocked indefinitely.
    for (lombok.val expired : repo.expireForDevice(deviceId, storeId)) {
      repo.log(
          expired.id(), deviceId, PdaFindStatus.EXPIRED.name(), "No completion before deadline");
    }
    UUID id = UUID.randomUUID();
    if (repo.create(id, requesterId, storeId, deviceId, Instant.now().plusSeconds(timeout)) == 0) {
      throw new DomainException(
          409,
          "This PDA already has an active finder request. Stop the current alarm or wait for it to"
              + " expire.");
    }
    repo.log(id, deviceId, PdaFindStatus.QUEUED.name(), null);
    ops.enqueue("FIND", id.toString(), "{\"storeId\":" + storeId + "}");
    audit(requesterId, storeId, "PDA_FIND", id);
    return FindPdaResult.from(found(repo.find(id, storeId)));
  }

  public FindPdaResult status(UUID id) {
    return FindPdaResult.from(found(repo.find(id, actor.get().storeId())));
  }

  public FcmHealthResult fcmHealth(long deviceId, String secret) {
    lombok.val device = identity.authenticate(deviceId, secret);
    if (!fcmEnabled) return new FcmHealthResult(true, "FCM_DISABLED");
    if (device.fcmToken() == null) return new FcmHealthResult(true, "NO_TOKEN");
    String last = repo.lastPushEvent(deviceId, device.storeId());
    boolean failed =
        "RETRY".equals(last) || "INVALID_TOKEN".equals(last) || "NO_TOKEN".equals(last);
    return new FcmHealthResult(failed, failed ? "PUSH_FAILED" : "READY");
  }

  public java.util.List<com.company.pda.application.pdafinder.dto.DeviceFinderCommand> commands(
      long deviceId, String secret) {
    lombok.val device = identity.authenticate(deviceId, secret);
    return repo.commands(deviceId, device.storeId()).stream()
        .map(
            r ->
                new com.company.pda.application.pdafinder.dto.DeviceFinderCommand(
                    r.id(),
                    java.util.Arrays.asList("QUEUED", "SENT", "RINGING").contains(r.status())
                        ? "FIND"
                        : "STOP",
                    r.expiresAt()))
        .collect(java.util.stream.Collectors.toList());
  }

  @Transactional
  public void stop(UUID id) {
    lombok.val a = actor.get();
    stopForStore(id, a.storeId(), a.id());
  }

  public FindPdaResult statusFromWeb(UUID id) {
    return FindPdaResult.from(found(repo.findAny(id)));
  }

  @Transactional
  public void stopFromWeb(UUID id) {
    stopForStore(id, found(repo.findAny(id)).storeId(), null);
  }

  private void stopForStore(UUID id, long storeId, Long requesterId) {
    lombok.val r = found(repo.find(id, storeId));
    if (repo.status(id, PdaFindStatus.STOPPED.name()) == 1) {
      repo.log(id, r.deviceId(), "STOP_REQUESTED", null);
      ops.enqueue("STOP", id.toString(), "{\"storeId\":" + storeId + "}");
      audit(requesterId, storeId, "PDA_STOP", id);
    }
  }

  private void audit(Long requesterId, long storeId, String action, UUID id) {
    if (requesterId == null) ops.systemAudit(storeId, action + "_WEB", id.toString());
    else ops.audit(requesterId, storeId, action, id.toString());
  }

  @Transactional
  public void event(long deviceId, String secret, PdaAlertEventCommand body) {
    lombok.val d = identity.authenticate(deviceId, secret);
    lombok.val r = found(repo.find(body.requestId(), d.storeId()));
    if (r.deviceId() != deviceId) throw new DomainException(403, "Wrong target device");
    String status = body.status();
    if (status.equals(PdaFindStatus.RINGING.name()) && !r.expiresAt().isAfter(Instant.now()))
      status = PdaFindStatus.EXPIRED.name();
    if (repo.status(r.id(), status) == 1) repo.log(r.id(), deviceId, status, null);
  }
}
