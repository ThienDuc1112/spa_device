package com.company.pda.application.device.service;

import com.company.pda.application.device.dto.DeviceRegistrationResult;
import com.company.pda.application.device.dto.DeviceResult;
import com.company.pda.application.device.dto.RegisterDeviceCommand;
import com.company.pda.application.device.dto.UpdateFcmTokenCommand;
import com.company.pda.application.device.usecase.DeviceUseCase;
import com.company.pda.application.port.out.AuditLogPort;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.domain.device.model.Device;
import com.company.pda.domain.device.repository.DeviceRepository;
import com.company.pda.domain.shared.exception.DomainException;
import com.company.pda.domain.shared.model.SecretHash;
import java.security.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService implements DeviceUseCase {
  private final DeviceRepository repo;
  private final CurrentActor actor;
  private final AuditLogPort ops;

  public DeviceService(DeviceRepository repo, CurrentActor actor, AuditLogPort ops) {
    this.repo = repo;
    this.actor = actor;
    this.ops = ops;
  }

  public java.util.List<DeviceResult> list() {
    return repo.list(actor.get().storeId()).stream()
        .map(DeviceResult::from)
        .collect(java.util.stream.Collectors.toList());
  }

  @Transactional
  public DeviceRegistrationResult register(RegisterDeviceCommand body) {
    lombok.val a = actor.get();
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    long id =
        repo.register(
            body.deviceCode(),
            body.deviceName(),
            a.storeId(),
            a.id(),
            SecretHash.hash(secret),
            body.fcmToken());
    ops.audit(a.id(), a.storeId(), "DEVICE_REGISTER", "" + id);
    return new DeviceRegistrationResult(id, secret);
  }

  public Device authenticate(long id, String secret) {
    lombok.val d = repo.identity(id);
    if (d == null
        || secret == null
        || !MessageDigest.isEqual(
            d.credentialHash().getBytes(java.nio.charset.StandardCharsets.UTF_8),
            SecretHash.hash(secret).getBytes(java.nio.charset.StandardCharsets.UTF_8)))
      throw new DomainException(401, "Invalid device credential");
    return d;
  }

  @Transactional
  public void token(long id, String secret, UpdateFcmTokenCommand body) {
    lombok.val d = authenticate(id, secret);
    repo.token(id, body.fcmToken());
    ops.systemAudit(d.storeId(), "DEVICE_TOKEN_REFRESH", Long.toString(id));
  }
}
