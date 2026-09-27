package com.company.pda.application.auth.dto;

public record TokenResult(String accessToken, String refreshToken, long expiresIn) {}
