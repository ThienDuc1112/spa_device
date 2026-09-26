package com.company.pda.common.result;

public record Result<T>(T data, boolean cached) {}
