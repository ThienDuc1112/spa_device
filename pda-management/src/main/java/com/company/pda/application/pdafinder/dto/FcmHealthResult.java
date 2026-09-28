package com.company.pda.application.pdafinder.dto;

/** Last observed FCM transport state, not a guarantee that a message reached the PDA. */
public record FcmHealthResult(boolean fallbackRequired, String reason) {}
