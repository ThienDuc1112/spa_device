package com.company.pda.domain.device.repository;

import com.company.pda.domain.device.model.Device;

public interface DeviceRepository {
  Device find(long id, long storeId);

  Device identity(long id);

  java.util.List<Device> list(long storeId);

  Long register(String code, String name, long storeId, long actorId, String hash, String token);

  int token(long id, String token);

  int invalidate(long id, String token);
}
