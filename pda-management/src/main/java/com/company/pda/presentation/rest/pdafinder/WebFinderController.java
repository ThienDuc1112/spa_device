package com.company.pda.presentation.rest.pdafinder;

import com.company.pda.application.device.service.DeviceService;
import com.company.pda.application.pdafinder.dto.FinderConfiguration;
import com.company.pda.application.pdafinder.dto.WebDeviceRow;
import com.company.pda.application.pdafinder.service.PdaFinderService;
import com.company.pda.application.port.out.WebFinderQueries;
import com.company.pda.presentation.rest.pdafinder.dto.FindPdaResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Deliberately public finder API for the internal website; other APIs retain authentication. */
@RestController
@RequestMapping("/web/finder")
public class WebFinderController {
  private final WebFinderQueries queries;
  private final PdaFinderService finder;
  private final DeviceService devices;
  private final FinderConfiguration configuration;

  public WebFinderController(
      WebFinderQueries queries,
      PdaFinderService finder,
      DeviceService devices,
      @org.springframework.beans.factory.annotation.Value("${app.fcm-enabled:false}")
          boolean fcmEnabled,
      @org.springframework.beans.factory.annotation.Value("${app.scheduler-enabled:true}")
          boolean schedulerEnabled) {
    this.queries = queries;
    this.finder = finder;
    this.devices = devices;
    this.configuration = new FinderConfiguration(fcmEnabled, schedulerEnabled);
  }

  @GetMapping("/configuration")
  public ResponseEntity<FinderConfiguration> configuration() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(configuration);
  }

  @DeleteMapping("/devices/{deviceId}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long deviceId) {
    devices.deleteFromWeb(deviceId);
  }

  @GetMapping("/devices")
  public ResponseEntity<List<WebDeviceRow>> devices() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(queries.devices());
  }

  @PostMapping("/devices/{deviceId}/find")
  public FindPdaResponse find(@PathVariable long deviceId) {
    return FindPdaResponse.from(finder.findFromWeb(deviceId));
  }

  @GetMapping("/requests/{id}")
  public ResponseEntity<FindPdaResponse> status(@PathVariable UUID id) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(FindPdaResponse.from(finder.statusFromWeb(id)));
  }

  @PostMapping("/requests/{id}/stop")
  public void stop(@PathVariable UUID id) {
    finder.stopFromWeb(id);
  }
}
