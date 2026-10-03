package com.company.pda.infrastructure.firebase;

import static org.junit.Assert.*;

import com.company.pda.common.exception.ApiException;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto.Command;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class FinderPollingCycleTest {
  private static class Server implements FinderPollingCycle.Transport {
    IOException error;
    int commandCalls;

    public List<Command> commands() throws IOException {
      commandCalls++;
      if (error != null) throw error;
      return List.of(new Command());
    }
  }

  @Test
  public void fcmModeNeverContactsCommandEndpoint() throws Exception {
    var server = new Server();
    server.error = new IOException("offline");
    var cycle = new FinderPollingCycle(server, false);
    assertTrue(cycle.run().isEmpty());
    assertTrue(cycle.run().isEmpty());
    assertEquals(0, server.commandCalls);
  }

  @Test
  public void explicitPollingFetchesEveryCycleWithoutFcmHealth() throws Exception {
    var server = new Server();
    var cycle = new FinderPollingCycle(server, true);
    assertEquals(1, cycle.run().size());
    assertEquals(1, cycle.run().size());
    assertEquals(2, server.commandCalls);
    assertEquals(5, cycle.delaySeconds());
  }

  @Test
  public void networkFailureDoesNotChangeConfiguredTransport() throws Exception {
    var server = new Server();
    var cycle = new FinderPollingCycle(server, true);
    server.error = new IOException("offline");
    assertThrows(IOException.class, cycle::run);
    server.error = null;
    assertEquals(1, cycle.run().size());
    assertEquals(2, server.commandCalls);
  }

  @Test
  public void authenticationFailureIsPropagatedSoServiceCanStop() {
    var server = new Server();
    server.error = new ApiException(401, "Revoked credential");
    var cycle = new FinderPollingCycle(server, true);
    assertSame(server.error, assertThrows(ApiException.class, cycle::run));
  }
}
