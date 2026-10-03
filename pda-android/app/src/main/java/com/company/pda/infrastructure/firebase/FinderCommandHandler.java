package com.company.pda.infrastructure.firebase;

import com.company.pda.PdaApplication;
import com.company.pda.domain.usecase.StartPdaAlarmUseCase;
import com.company.pda.domain.usecase.StopPdaAlarmUseCase;
import com.company.pda.infrastructure.alarm.PdaAlarmService;

/** Called on the main thread by both transports, including STOP tombstones. */
public final class FinderCommandHandler {
  public static void handle(
      PdaApplication app, String id, String command, String expiresAt, boolean mayStart) {
    try {
      java.util.UUID.fromString(id);
    } catch (Exception e) {
      return;
    }
    if ("STOP".equals(command)) {
      new StopPdaAlarmUseCase(app.modules().finder).execute(id);
      return;
    }
    if (!"FIND".equals(command)
        || app.modules().tokens.get("handled:" + id) != null
        || id.equals(PdaAlarmService.activeId)) return;
    long expiry;
    try {
      expiry = java.time.Instant.parse(expiresAt).toEpochMilli();
    } catch (Exception e) {
      return;
    }
    if (expiry <= System.currentTimeMillis()) return;
    if (!mayStart) {
      PdaAlarmService.notifyFallback(app, id);
      return;
    }
    try {
      new StartPdaAlarmUseCase(app.modules().finder).execute(id, expiry);
    } catch (RuntimeException e) {
      PdaAlarmService.notifyFallback(app, id);
      // Do not mark handled: a later delivery in the configured transport may retry before expiry.
    }
  }
}
