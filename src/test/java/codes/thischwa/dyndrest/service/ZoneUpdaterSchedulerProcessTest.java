package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.provider.Provider;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ZoneUpdaterSchedulerProcessTest {

  private static final String ZONE = "example.com";

  @Test
  void process_DiscardsUpdateForDeletedHost() throws Exception {
    UpdateLogService updateLogService = mock(UpdateLogService.class);
    HostZoneService hostZoneService = mock(HostZoneService.class);
    Provider provider = mock(Provider.class);
    when(hostZoneService.hostExists(anyString())).thenReturn(true);
    ZoneUpdaterService zoneUpdaterService = new ZoneUpdaterService(updateLogService, hostZoneService);
    HostInfoHolder current = createHost("www", "192.0.2.1");
    HostInfoHolder update = createHost("www", "192.0.2.2");
    zoneUpdaterService.addCurrentHosts(current);
    zoneUpdaterService.addOrUpdateHost(update);
    when(hostZoneService.hostExists(current.getFullHost())).thenReturn(false);

    new ZoneUpdaterScheduler(zoneUpdaterService, hostZoneService, provider).process();

    verify(provider, never()).patch(anyString(), any(), any(), any());
    verify(updateLogService).log(update.getFullHost(), update.getIpSetting(), UpdateLog.Status.failed);
    assertFalse(zoneUpdaterService.hostExists(update.getFullHost()));
    assertTrue(zoneUpdaterService.getHostsToUpdate(ZONE).isEmpty());
  }

  @Test
  void process_PatchesOnlyUpdates() throws Exception {
    UpdateLogService updateLogService = mock(UpdateLogService.class);
    HostZoneService hostZoneService = mock(HostZoneService.class);
    Provider provider = mock(Provider.class);
    when(hostZoneService.hostExists(anyString())).thenReturn(true);
    ZoneUpdaterService zoneUpdaterService = new ZoneUpdaterService(updateLogService, hostZoneService);
    HostInfoHolder current = createHost("www", "192.0.2.1");
    HostInfoHolder update = createHost("www", "192.0.2.2");
    zoneUpdaterService.addCurrentHosts(current);
    zoneUpdaterService.addOrUpdateHost(update);

    new ZoneUpdaterScheduler(zoneUpdaterService, hostZoneService, provider).process();

    verify(provider).patch(eq(ZONE), eq((List<HostInfoHolder>) null), eq(List.of(update)),
        eq((List<String>) null));
    assertTrue(zoneUpdaterService.hostExists(update.getFullHost()));
    assertTrue(zoneUpdaterService.getHostsToUpdate(ZONE).isEmpty());
    verify(updateLogService).log(update.getFullHost(), update.getIpSetting(), UpdateLog.Status.success);
  }

  private HostInfoHolder createHost(String sld, String ipv4) throws UnknownHostException {
    HostInfoHolder host = new HostInfoHolder();
    host.setSld(sld);
    host.setZone(ZONE);
    host.setIpSetting(new IpSetting(InetAddress.getByName(ipv4), null));
    return host;
  }
}