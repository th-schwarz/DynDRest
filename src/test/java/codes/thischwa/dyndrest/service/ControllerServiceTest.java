package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ControllerServiceTest {

  private UpdateLogService updateLogService;
  private ZoneUpdaterService zoneUpdaterService;
  private AppConfig appConfig;
  private ControllerService controllerService;

  @BeforeEach
  void setUp() {
    updateLogService = mock(UpdateLogService.class);
    zoneUpdaterService = mock(ZoneUpdaterService.class);
    appConfig = mock(AppConfig.class);
    when(appConfig.zoneUpdateSchedulerEnabled()).thenReturn(true);
    controllerService = new ControllerService(updateLogService, zoneUpdaterService,
        mock(HostZoneService.class), appConfig, mock(Provider.class));
  }

  @Test
  void processIpUpdate_unknownHostReturnsNotFoundWithoutWaitingLog() throws Exception {
    HostInfoHolder host = new HostInfoHolder();
    host.setSld("deleted");
    host.setZone("example.com");
    IpSetting ipSetting = new IpSetting("192.0.2.1");
    host.setIpSetting(ipSetting);
    doThrow(new IllegalArgumentException("Host not found"))
        .when(zoneUpdaterService).addOrUpdateHost(any(HostInfoHolder.class));
    doAnswer(invocation -> {
      ((Runnable) invocation.getArgument(0)).run();
      return null;
    }).when(zoneUpdaterService).runExclusive(any(Runnable.class));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controllerService.processIpUpdate(host));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(updateLogService, never()).log(host.getFullHost(), ipSetting, UpdateLog.Status.waiting);
  }
}
