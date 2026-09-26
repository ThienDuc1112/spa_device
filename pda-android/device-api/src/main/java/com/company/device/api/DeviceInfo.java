package com.company.device.api;

import java.util.Set;

public record DeviceInfo(
    String manufacturer, String model, int apiLevel, Set<DeviceCapability> capabilities) {}
