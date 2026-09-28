package com.company.pda.application.pdafinder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.company.pda.application.device.usecase.DeviceUseCase;
import com.company.pda.application.pdafinder.service.PdaFinderService;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.application.port.out.OperationsRepository;
import com.company.pda.domain.device.model.Device;
import com.company.pda.domain.device.repository.DeviceRepository;
import com.company.pda.domain.pdafinder.repository.PdaFindRepository;
import org.junit.jupiter.api.Test;

class FcmHealthTest {
  private final PdaFindRepository requests = mock(PdaFindRepository.class);
  private final DeviceUseCase identity = mock(DeviceUseCase.class);

  private PdaFinderService service(boolean enabled, String token) {
    when(identity.authenticate(1, "secret"))
        .thenReturn(new Device(1, "PDA1", "PDA", 7, "hash", token, null));
    return new PdaFinderService(
        requests,
        mock(DeviceRepository.class),
        identity,
        mock(OperationsRepository.class),
        mock(CurrentActor.class),
        60,
        enabled);
  }

  @Test
  void disabledFcmAndMissingTokenRequireFallback() {
    assertEquals("FCM_DISABLED", service(false, "token").fcmHealth(1, "secret").reason());
    assertEquals("NO_TOKEN", service(true, null).fcmHealth(1, "secret").reason());
    verifyNoInteractions(requests);
  }

  @Test
  void lastObservedSendFailureEnablesFallbackUntilSendSucceeds() {
    var service = service(true, "token");
    assertFalse(service.fcmHealth(1, "secret").fallbackRequired());
    for (String event : java.util.List.of("RETRY", "INVALID_TOKEN", "NO_TOKEN")) {
      when(requests.lastPushEvent(1, 7)).thenReturn(event);
      assertTrue(service.fcmHealth(1, "secret").fallbackRequired());
    }
    for (String event : java.util.List.of("PUSH_FIND", "PUSH_STOP")) {
      when(requests.lastPushEvent(1, 7)).thenReturn(event);
      assertFalse(service.fcmHealth(1, "secret").fallbackRequired());
    }
  }
}
