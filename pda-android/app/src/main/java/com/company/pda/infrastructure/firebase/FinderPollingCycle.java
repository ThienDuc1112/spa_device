package com.company.pda.infrastructure.firebase;

import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto.Command;
import java.io.IOException;
import java.util.List;

/** Command polling is explicitly configured; FCM health never selects the transport. */
public final class FinderPollingCycle {
  public interface Transport {
    List<Command> commands() throws IOException;
  }

  private final Transport transport;
  private final boolean enabled;

  public FinderPollingCycle(Transport transport, boolean enabled) {
    this.transport = transport;
    this.enabled = enabled;
  }

  public List<Command> run() throws IOException {
    return enabled ? transport.commands() : List.of();
  }

  public int delaySeconds() {
    return 5;
  }
}
