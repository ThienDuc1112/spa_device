package com.company.pda.application.pdafinder.service;

import com.company.pda.application.port.out.NotificationPort;
import com.company.pda.application.port.out.OperationsRepository;
import com.company.pda.domain.device.repository.DeviceRepository;
import com.company.pda.domain.pdafinder.model.PdaFindStatus;
import com.company.pda.domain.pdafinder.repository.PdaFindRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxProcessor {
  private final OperationsRepository ops;
  private final PdaFindRepository finder;
  private final DeviceRepository devices;
  private final NotificationPort push;
  private final ObjectMapper json;

  public OutboxProcessor(
      OperationsRepository ops,
      PdaFindRepository finder,
      DeviceRepository devices,
      NotificationPort push,
      ObjectMapper json) {
    this.ops = ops;
    this.finder = finder;
    this.devices = devices;
    this.push = push;
    this.json = json;
  }

  @Transactional
  public void processOne() throws Exception {
    var event = ops.next();
    if (event == null) return;
    var request =
        finder.find(
            UUID.fromString(event.aggregateId()),
            json.readTree(event.payload()).get("storeId").asLong());
    if (request == null
        || (!event.eventType().equals("STOP")
            && (!request.expiresAt().isAfter(Instant.now())
                || java.util.Set.of(
                        PdaFindStatus.STOPPED.name(),
                        PdaFindStatus.EXPIRED.name(),
                        PdaFindStatus.FAILED.name())
                    .contains(request.status())))) {
      ops.done(event.id());
      return;
    }
    var device = devices.find(request.deviceId(), request.storeId());
    if (device == null || device.fcmToken() == null) {
      finder.status(request.id(), PdaFindStatus.FAILED.name());
      finder.log(request.id(), request.deviceId(), "NO_TOKEN", null);
      ops.done(event.id());
      return;
    }
    try {
      push.send(device.fcmToken(), event.eventType(), request.id(), request.expiresAt());
      if (event.eventType().equals("FIND")) finder.sent(request.id());
      finder.log(request.id(), device.id(), "PUSH_" + event.eventType(), null);
      ops.done(event.id());
    } catch (NotificationPort.InvalidToken e) {
      devices.invalidate(device.id(), device.fcmToken());
      finder.status(request.id(), PdaFindStatus.FAILED.name());
      finder.log(request.id(), device.id(), "INVALID_TOKEN", null);
      ops.done(event.id());
    } catch (RuntimeException e) {
      finder.log(request.id(), device.id(), "RETRY", "Push unavailable");
      if (event.attempts() >= 7 || !request.expiresAt().isAfter(Instant.now())) {
        finder.status(request.id(), PdaFindStatus.FAILED.name());
        ops.done(event.id());
      } else ops.retry(event.id());
    }
  }

  @Transactional
  public void expire() {
    for (var r : finder.expired()) {
      finder.status(r.id(), PdaFindStatus.EXPIRED.name());
      finder.log(
          r.id(), r.deviceId(), PdaFindStatus.EXPIRED.name(), "No completion before deadline");
    }
  }
}
