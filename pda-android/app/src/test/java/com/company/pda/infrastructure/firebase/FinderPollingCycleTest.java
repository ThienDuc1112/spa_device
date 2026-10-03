package com.company.pda.infrastructure.firebase;

import static org.junit.Assert.*;

import com.company.pda.common.exception.ApiException;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto.Command;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class FinderPollingCycleTest {
  private static class Server implements FinderPollingCycle.Transport {
    boolean failed;
    IOException healthError;
    int healthCalls, commandCalls;

    public boolean backendFcmFailed() throws IOException {
      healthCalls++;
      if (healthError != null) throw healthError;
      return failed;
    }

    public List<Command> commands() {
      commandCalls++;
      return List.of(new Command());
    }
  }

  @Test
  public void healthyPushNeverFetchesCommandsAndFailureThenRecoverySwitchesTransport()
      throws Exception {
    var server = new Server();
    var cycle = new FinderPollingCycle(server);
    assertTrue(cycle.run(false, 0).isEmpty());
    cycle.run(false, 15_000);
    assertEquals(0, server.commandCalls);
    assertEquals(15, cycle.delaySeconds());
    server.failed = true;
    assertEquals(1, cycle.run(false, 30_000).size());
    cycle.run(false, 35_000);
    cycle.run(false, 40_000);
    assertEquals(3, server.commandCalls);
    assertEquals(3, server.healthCalls);
    assertEquals(5, cycle.delaySeconds());
    server.failed = false;
    assertTrue(cycle.run(false, 45_000).isEmpty());
    assertEquals(3, server.commandCalls);
    assertEquals(15, cycle.delaySeconds());
  }

  @Test
  public void localTokenFailureEnablesPollingEvenWhenBackendReportsHealthy() throws Exception {
    var server = new Server();
    var cycle = new FinderPollingCycle(server);
    cycle.run(true, 0);
    cycle.run(true, 5_000);
    assertEquals(2, server.commandCalls);
    assertTrue(cycle.run(false, 10_000).isEmpty());
    assertEquals(2, server.commandCalls);
  }

  @Test
  public void healthNetworkFailureDoesNotEnablePollingButPreservesKnownFcmFailure()
      throws Exception {
    var server = new Server();
    var cycle = new FinderPollingCycle(server);
    server.healthError = new IOException("offline");
    assertThrows(IOException.class, () -> cycle.run(false, 0));
    assertEquals(0, server.commandCalls);
    server.healthError = null;
    server.failed = true;
    cycle.run(false, 15_000);
    server.healthError = new IOException("offline");
    cycle.run(false, 30_000);
    assertEquals(2, server.commandCalls);
  }

  @Test
  public void authenticationFailureNeverFetchesCommandsEvenWithLocalFcmFailure() {
    var server = new Server();
    server.healthError = new ApiException(401, "Revoked credential");
    var cycle = new FinderPollingCycle(server);
    assertThrows(ApiException.class, () -> cycle.run(true, 0));
    assertEquals(0, server.commandCalls);
  }
}
