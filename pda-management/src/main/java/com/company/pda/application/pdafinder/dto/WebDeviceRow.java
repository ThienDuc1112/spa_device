package com.company.pda.application.pdafinder.dto;

import java.time.Instant;
import java.util.UUID;

/** Public dashboard projection. Never includes tokens or device credentials. */
public class WebDeviceRow {
  public long storeId;
  public String storeCode;
  public String storeName;
  public Long deviceId;
  public String deviceCode;
  public String deviceName;
  public Instant lastActiveAt;
  public boolean hasPushToken;
  public UUID requestId;
  public String status;
  public Instant expiresAt;
  public String lastEvent;
  public String lastEventMessage;
}
