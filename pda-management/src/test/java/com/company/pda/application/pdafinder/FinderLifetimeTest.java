package com.company.pda.application.pdafinder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.company.pda.application.device.usecase.DeviceUseCase;
import com.company.pda.application.pdafinder.service.PdaFinderService;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.application.port.out.OperationsRepository;
import com.company.pda.domain.device.model.Device;
import com.company.pda.domain.device.repository.DeviceRepository;
import com.company.pda.domain.pdafinder.model.PdaFindRequest;
import com.company.pda.domain.pdafinder.repository.PdaFindRepository;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class FinderLifetimeTest {
  @Test
  void fifteenMinuteRequestRemainsValidAfterTenMinutesOffline() {
    Instant before = Instant.now();
    PdaFindRequest request = createRequest(900);
    Instant after = Instant.now();
    assertFalse(request.expiresAt().isBefore(before.plusSeconds(900)));
    assertFalse(request.expiresAt().isAfter(after.plusSeconds(900)));
    assertTrue(request.expiresAt().isAfter(after.plusSeconds(600)));
  }

  @Test
  void shorterConfiguredLifetimeAndFifteenMinuteMaximumAreRespected() {
    Instant before = Instant.now();
    PdaFindRequest shortRequest = createRequest(60);
    PdaFindRequest cappedRequest = createRequest(3600);
    Instant after = Instant.now();
    assertFalse(shortRequest.expiresAt().isBefore(before.plusSeconds(60)));
    assertFalse(shortRequest.expiresAt().isAfter(after.plusSeconds(60)));
    assertFalse(cappedRequest.expiresAt().isBefore(before.plusSeconds(900)));
    assertFalse(cappedRequest.expiresAt().isAfter(after.plusSeconds(900)));
  }

  private PdaFindRequest createRequest(int timeout) {
    DeviceRepository devices = mock(DeviceRepository.class);
    PdaFindRepository requests = mock(PdaFindRepository.class);
    Device device = new Device(1, "PDA1", "PDA", 7, "hash", "token", null);
    when(devices.identity(1)).thenReturn(device);
    when(devices.find(1, 7)).thenReturn(device);
    AtomicReference<PdaFindRequest> created = new AtomicReference<>();
    when(requests.create(any(UUID.class), isNull(), eq(7L), eq(1L), any(Instant.class)))
        .thenAnswer(
            call -> {
              created.set(
                  new PdaFindRequest(
                      call.getArgument(0), null, 7, 1, "QUEUED", call.getArgument(4)));
              return 1;
            });
    when(requests.find(any(UUID.class), eq(7L))).thenAnswer(call -> created.get());
    PdaFinderService service =
        new PdaFinderService(
            requests,
            devices,
            mock(DeviceUseCase.class),
            mock(OperationsRepository.class),
            mock(CurrentActor.class),
            timeout,
            true);
    service.findFromWeb(1);
    return created.get();
  }
}
