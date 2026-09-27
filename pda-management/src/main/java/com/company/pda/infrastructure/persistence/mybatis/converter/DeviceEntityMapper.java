package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.device.model.Device;
import com.company.pda.infrastructure.persistence.mybatis.entity.DeviceEntity;

public final class DeviceEntityMapper {
  private DeviceEntityMapper() {}

  public static Device toDomain(DeviceEntity row) {
    return row == null
        ? null
        : new Device(
            row.id(),
            row.deviceCode(),
            row.deviceName(),
            row.storeId(),
            row.credentialHash(),
            row.fcmToken(),
            row.lastActiveAt());
  }

  public static DeviceEntity toEntity(Device model) {
    return model == null
        ? null
        : new DeviceEntity(
            model.id(),
            model.deviceCode(),
            model.deviceName(),
            model.storeId(),
            model.credentialHash(),
            model.fcmToken(),
            model.lastActiveAt());
  }
}
