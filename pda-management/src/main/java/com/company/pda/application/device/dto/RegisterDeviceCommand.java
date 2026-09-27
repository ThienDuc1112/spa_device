package com.company.pda.application.device.dto;

public record RegisterDeviceCommand(String deviceCode, String deviceName, String fcmToken) {}
