package com.company.pda.infrastructure.firebase;

import com.company.pda.common.exception.ApiException;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto.Command;
import java.io.IOException;
import java.util.List;

/** Separates health checks from command polling; HTTP errors alone never imply an FCM failure. */
public final class FinderPollingCycle {
  public interface Transport {
    boolean backendFcmFailed() throws IOException;

    List<Command> commands() throws IOException;
  }

  private final Transport transport;
  private boolean backendFailed;
  private long nextHealthCheck;
  private int delaySeconds = 15;

  public FinderPollingCycle(Transport transport) {
    this.transport = transport;
  }

  public List<Command> run(boolean localFcmFailed, long elapsedMillis) throws IOException {
    if (elapsedMillis >= nextHealthCheck) {
      nextHealthCheck = elapsedMillis + 15_000;
      try {
        backendFailed = transport.backendFcmFailed();
      } catch (IOException error) {
        if (error instanceof ApiException api && (api.status == 401 || api.status == 403))
          throw error;
        // Preserve a previously confirmed failure; an unavailable health endpoint is not proof
        // that FCM failed or recovered.
        if (!localFcmFailed && !backendFailed) throw error;
      }
    }
    if (!localFcmFailed && !backendFailed) {
      delaySeconds = 15;
      return List.of();
    }
    delaySeconds = 5;
    return transport.commands();
  }

  public int delaySeconds() {
    return delaySeconds;
  }
}
