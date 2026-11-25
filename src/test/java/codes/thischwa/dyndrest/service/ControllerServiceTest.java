package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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
import codes.thischwa.dyndrest.provider.ProviderException;
import java.net.UnknownHostException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ControllerServiceTest {

  private UpdateLogService updateLogService;
  private ZoneUpdaterService zoneUpdaterService;
  private HostZoneService hostZoneService;
  private Provider provider;
  private ControllerService controllerService;

  @BeforeEach
  void setUp() {
    updateLogService = mock(UpdateLogService.class);
    hostZoneService = mock(HostZoneService.class);
    when(hostZoneService.hostExists(anyString())).thenReturn(true);
    zoneUpdaterService = new ZoneUpdaterService(updateLogService, hostZoneService);
    AppConfig appConfig = mock(AppConfig.class);
    when(appConfig.zoneUpdateSchedulerEnabled()).thenReturn(true);
    provider = mock(Provider.class);
    controllerService = new ControllerService(updateLogService, zoneUpdaterService,
        hostZoneService, appConfig, provider);
  }

  @Test
  void processIpUpdate_InitialAssignmentIsSynchronous() throws Exception {
    HostInfoHolder host = createHost("www", "192.0.2.1");

    controllerService.processIpUpdate(host);

    verify(provider).addOrUpdate(host.getFullHost(), host.getIpSetting());
    assertTrue(zoneUpdaterService.hostExists(host.getFullHost()));
    verify(updateLogService).log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.success);
    verify(updateLogService, never()).log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.waiting);
  }

  @Test
  void processIpUpdate_KnownHostUpdateIsQueued() throws Exception {
    HostInfoHolder current = createHost("www", "192.0.2.1");
    HostInfoHolder update = createHost("www", "192.0.2.2");
    zoneUpdaterService.addCurrentHosts(current);

    controllerService.processIpUpdate(update);

    verify(provider, never()).addOrUpdate(anyString(), org.mockito.ArgumentMatchers.any(IpSetting.class));
    assertEquals(1, zoneUpdaterService.getHostsToUpdate("example.com").size());
    verify(updateLogService).log(update.getFullHost(), update.getIpSetting(), UpdateLog.Status.waiting);
  }

  @Test
  void processIpUpdate_SameIpDoesNotLogWaiting() throws Exception {
    HostInfoHolder current = createHost("www", "192.0.2.1");
    zoneUpdaterService.addCurrentHosts(current);

    controllerService.processIpUpdate(current);

    assertTrue(zoneUpdaterService.getHostsToUpdate("example.com").isEmpty());
    verify(updateLogService, never()).log(current.getFullHost(), current.getIpSetting(), UpdateLog.Status.waiting);
  }

  @Test
  void processIpUpdate_ProviderFailureReturnsServerErrorWithoutCachingHost() throws Exception {
    HostInfoHolder host = createHost("www", "192.0.2.1");
    doThrow(new ProviderException("provider failure"))
        .when(provider).addOrUpdate(host.getFullHost(), host.getIpSetting());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controllerService.processIpUpdate(host));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertFalse(zoneUpdaterService.hostExists(host.getFullHost()));
    verify(updateLogService).log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.failed);
    verify(updateLogService, never()).log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.waiting);
  }

  @Test
  void processIpUpdate_RecreatedHostUsesSynchronousInitialAssignment() throws Exception {
    HostInfoHolder host = createHost("www", "192.0.2.1");
    zoneUpdaterService.addCurrentHosts(host);
    zoneUpdaterService.purgeHost(host.getFullHost());

    controllerService.processIpUpdate(host);

    verify(provider).addOrUpdate(host.getFullHost(), host.getIpSetting());
    assertTrue(zoneUpdaterService.hostExists(host.getFullHost()));
  }

  @Test
  void processIpUpdate_UnknownHostReturnsNotFound() throws Exception {
    HostInfoHolder host = createHost("deleted", "192.0.2.1");
    when(hostZoneService.hostExists(host.getFullHost())).thenReturn(false);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controllerService.processIpUpdate(host));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(updateLogService, never()).log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.waiting);
  }

  private HostInfoHolder createHost(String sld, String ipv4) throws UnknownHostException {
    HostInfoHolder host = new HostInfoHolder();
    host.setSld(sld);
    host.setZone("example.com");
    host.setIpSetting(new IpSetting(ipv4));
    return host;
  }
}
