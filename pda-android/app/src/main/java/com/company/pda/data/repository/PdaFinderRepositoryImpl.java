package com.company.pda.data.repository;

import static com.company.pda.common.util.ApiCalls.execute;

import com.company.pda.data.local.preferences.TokenStorage;
import com.company.pda.data.remote.api.PdaFinderApi;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto;
import com.company.pda.domain.repository.PdaFinderRepository;
import com.company.pda.infrastructure.alarm.AlarmController;
import com.company.pda.infrastructure.device.DeviceManager;

public class PdaFinderRepositoryImpl implements PdaFinderRepository {
  private final PdaFinderApi api;
  private final TokenStorage tokens;
  private final DeviceManager device;
  private final AlarmController alarm;

  public PdaFinderRepositoryImpl(
      PdaFinderApi api, TokenStorage tokens, DeviceManager device, AlarmController alarm) {
    this.api = api;
    this.tokens = tokens;
    this.device = device;
    this.alarm = alarm;
  }

  public void register(String code, String name) throws java.io.IOException {
    String token = tokens.get("fcmToken");
    var r = execute(api.register(new PdaFinderDto.Register(code, name, token)));
    device.registered(r.deviceId, r.deviceSecret);
  }

  public void startLocal(String id, long expiry) {
    alarm.start(id, expiry);
  }

  public void stopLocal(String id) {
    alarm.stop(id);
  }
}
