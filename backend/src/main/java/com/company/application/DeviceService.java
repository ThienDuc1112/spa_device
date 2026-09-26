package com.company.application;

import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import com.company.domain.Models.*;
import java.security.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService implements DeviceUseCase {
  private final DeviceRepository repo;
  private final CurrentActor actor;
  private final OperationsRepository ops;

  public DeviceService(DeviceRepository repo, CurrentActor actor, OperationsRepository ops) {
    this.repo = repo;
    this.actor = actor;
    this.ops = ops;
  }

  @Transactional
  public Registration register(Register body) {
    var a = actor.get();
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    long id =
        repo.register(
            body.deviceCode(),
            body.deviceName(),
            a.storeId(),
            a.id(),
            AuthService.hash(secret),
            body.fcmToken());
    ops.audit(a.id(), a.storeId(), "DEVICE_REGISTER", "" + id);
    return new Registration(id, secret);
  }

  public Device authenticate(long id, String secret) {
    var d = repo.identity(id);
    if (d == null
        || secret == null
        || !MessageDigest.isEqual(
            d.credentialHash().getBytes(java.nio.charset.StandardCharsets.UTF_8),
            AuthService.hash(secret).getBytes(java.nio.charset.StandardCharsets.UTF_8)))
      throw new BusinessException(401, "Invalid device credential");
    return d;
  }

  @Transactional
  public void token(long id, String secret, Token body) {
    var d = authenticate(id, secret);
    repo.token(id, body.fcmToken());
    ops.systemAudit(d.storeId(), "DEVICE_TOKEN_REFRESH", Long.toString(id));
  }
}
