package com.company.device.api;

import java.util.Set;

public final class DeviceInfo {
  private final String manufacturer;
  private final String model;
  private final int apiLevel;
  private final Set<DeviceCapability> capabilities;

  public DeviceInfo(
      String manufacturer, String model, int apiLevel, Set<DeviceCapability> capabilities) {
    this.manufacturer = manufacturer;
    this.model = model;
    this.apiLevel = apiLevel;
    this.capabilities = capabilities;
  }

  public String manufacturer() {
    return manufacturer;
  }

  public String model() {
    return model;
  }

  public int apiLevel() {
    return apiLevel;
  }

  public Set<DeviceCapability> capabilities() {
    return capabilities;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DeviceInfo other)) return false;
    return java.util.Objects.equals(manufacturer, other.manufacturer)
        && java.util.Objects.equals(model, other.model)
        && java.util.Objects.equals(apiLevel, other.apiLevel)
        && java.util.Objects.equals(capabilities, other.capabilities);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(manufacturer, model, apiLevel, capabilities);
  }

  @Override
  public String toString() {
    return "DeviceInfo[manufacturer="
        + manufacturer
        + ", model="
        + model
        + ", apiLevel="
        + apiLevel
        + ", capabilities="
        + capabilities
        + "]";
  }
}
