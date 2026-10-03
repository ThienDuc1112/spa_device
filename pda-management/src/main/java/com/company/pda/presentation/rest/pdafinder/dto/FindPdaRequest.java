package com.company.pda.presentation.rest.pdafinder.dto;

import com.company.pda.application.pdafinder.dto.FindPdaCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class FindPdaRequest {
  private final @Positive long deviceId;

  @java.beans.ConstructorProperties({"deviceId"})
  public FindPdaRequest(long deviceId) {
    this.deviceId = deviceId;
  }

  public long deviceId() {
    return deviceId;
  }

  public long getDeviceId() {
    return deviceId;
  }

  public FindPdaCommand toCommand() {
    return new FindPdaCommand(deviceId);
  }
}
