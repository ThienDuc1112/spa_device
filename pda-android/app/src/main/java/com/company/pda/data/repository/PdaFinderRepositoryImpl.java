package com.company.pda.data.repository;

import static com.company.pda.common.util.ApiCalls.execute;

import com.company.pda.data.local.preferences.TokenStorage;
import com.company.pda.data.remote.api.PdaFinderApi;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto;
import com.company.pda.domain.model.*;
import com.company.pda.domain.repository.PdaFinderRepository;
import com.company.pda.infrastructure.alarm.AlarmController;
import com.company.pda.infrastructure.device.DeviceManager;
import java.util.*;

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

  private PdaFindRequest map(PdaFinderDto.Request r) {
    var d = new PdaFindRequest();
    d.id = r.id;
    d.status = r.status;
    d.expiresAt = r.expiresAt;
    d.deviceId = r.deviceId;
    return d;
  }

  public List<Device> devices() throws java.io.IOException {
    var result = new ArrayList<Device>();
    for (var r : execute(api.devices())) {
      var d = new Device();
      d.id = r.id;
      d.deviceCode = r.deviceCode;
      d.deviceName = r.deviceName;
      d.lastActiveAt = r.lastActiveAt;
      d.reachable = r.reachable;
      result.add(d);
    }
    return result;
  }

  public PdaFindRequest find(long id) throws java.io.IOException {
    return map(execute(api.find(new PdaFinderDto.Find(id))));
  }

  public PdaFindRequest status(String id) throws java.io.IOException {
    return map(execute(api.status(id)));
  }

  public void stop(String id) throws java.io.IOException {
    execute(api.stop(new PdaFinderDto.Stop(id)));
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
