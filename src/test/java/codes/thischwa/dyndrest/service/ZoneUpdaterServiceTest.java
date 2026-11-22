package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZoneUpdaterServiceTest {

  static final String EXAMPLE_ZONE = "example.com";
  private ZoneUpdaterService zoneUpdaterService;
  private UpdateLogService updateLogService;
  private HostZoneService hostZoneService;

  @BeforeEach
  void setUp() {
    updateLogService = mock(UpdateLogService.class);
    hostZoneService = mock(HostZoneService.class);
    when(hostZoneService.hostExists(anyString())).thenReturn(true);
    zoneUpdaterService = new ZoneUpdaterService(updateLogService, hostZoneService);
  }

  @Test
  void testAddOrUpdateHost_NewHost() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdaterService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToCreate = zoneUpdaterService.getHostsToCreate(EXAMPLE_ZONE);
    assertEquals(1, hostsToCreate.size());
    assertEquals("www.example.com", hostsToCreate.get(0).getFullHost());
    assertTrue(zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE).isEmpty());
  }

  @Test
  void testAddOrUpdateHost_UpdateExistingHostWithDifferentIp() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("www", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdaterService.addOrUpdateHost(host1);
    zoneUpdaterService.deleteHost("www.example.com");
    zoneUpdaterService.afterUpdate(host1);
    zoneUpdaterService.addOrUpdateHost(host2);

    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE);
    assertEquals(1, hostsToUpdate.size());
    assertEquals("192.168.1.2", hostsToUpdate.get(0).getIpSetting().getIpv4().getHostAddress());
  }

  @Test
  void testAddOrUpdateHost_SameIpNoUpdate() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdaterService.addOrUpdateHost(host);
    zoneUpdaterService.deleteHost("www.example.com");
    zoneUpdaterService.afterUpdate(host);
    zoneUpdaterService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE);
    assertTrue(hostsToUpdate.isEmpty());
  }

  @Test
  void testDeleteHost() {
    zoneUpdaterService.deleteHost("www.example.com");

    List<String> hostsToDelete = zoneUpdaterService.getHostsToDelete(EXAMPLE_ZONE);
    assertEquals(1, hostsToDelete.size());
    assertEquals("www.example.com", hostsToDelete.get(0));
  }

  @Test
  void testAfterUpdate() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdaterService.addOrUpdateHost(host);
    zoneUpdaterService.deleteHost("www.example.com");
    zoneUpdaterService.afterUpdate(host);

    assertTrue(zoneUpdaterService.hostExists("www.example.com"));
    verify(updateLogService, times(1)).log(eq("www.example.com"), any(IpSetting.class), eq(UpdateLog.Status.success));
    assertTrue(zoneUpdaterService.getHostsToCreate(EXAMPLE_ZONE).isEmpty());
  }

  @Test
  void testAddOrUpdateHost_UnknownHost() throws UnknownHostException {
    HostInfoHolder host = createHost("deleted", EXAMPLE_ZONE, "192.168.1.1");
    when(hostZoneService.hostExists(host.getFullHost())).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> zoneUpdaterService.addOrUpdateHost(host));
    assertTrue(zoneUpdaterService.getHostsToCreate(EXAMPLE_ZONE).isEmpty());
  }

  @Test
  void testPurgeHost_RemovesPendingUpdateWithoutSchedulingDelete() throws UnknownHostException {
    HostInfoHolder existing = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder update = createHost("www", EXAMPLE_ZONE, "192.168.1.2");
    zoneUpdaterService.addCurrentHosts(existing);
    zoneUpdaterService.addOrUpdateHost(update);

    zoneUpdaterService.purgeHost("www.example.com");

    assertFalse(zoneUpdaterService.hostExists("www.example.com"));
    assertTrue(zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE).isEmpty());
    assertTrue(zoneUpdaterService.getHostsToDelete(EXAMPLE_ZONE).isEmpty());
    verify(updateLogService).log("www.example.com", update.getIpSetting(), UpdateLog.Status.failed);
  }

  @Test
  void testAfterUpdate_DeletedHostIsNotRestored() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    zoneUpdaterService.addCurrentHosts(host);
    when(hostZoneService.hostExists(host.getFullHost())).thenReturn(false);

    zoneUpdaterService.afterUpdate(host);

    assertFalse(zoneUpdaterService.hostExists(host.getFullHost()));
    verify(updateLogService).log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.failed);
  }

  @Test
  void testHostExists() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    assertFalse(zoneUpdaterService.hostExists("www.example.com"));

    zoneUpdaterService.addOrUpdateHost(host);
    zoneUpdaterService.deleteHost("www.example.com");
    zoneUpdaterService.afterUpdate(host);

    assertTrue(zoneUpdaterService.hostExists("www.example.com"));
  }

  @Test
  void testRemoveFromCurrentHosts() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdaterService.addOrUpdateHost(host);
    zoneUpdaterService.deleteHost("www.example.com");
    zoneUpdaterService.afterUpdate(host);
    assertTrue(zoneUpdaterService.hostExists("www.example.com"));

    zoneUpdaterService.removeFromCurrentHosts("www.example.com");
    assertFalse(zoneUpdaterService.hostExists("www.example.com"));
  }

  @Test
  void testGetCurrentHosts() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("mail", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdaterService.addOrUpdateHost(host1);
    zoneUpdaterService.deleteHost("www.example.com");
    zoneUpdaterService.afterUpdate(host1);
    zoneUpdaterService.addOrUpdateHost(host2);
    zoneUpdaterService.deleteHost("mail.example.com");
    zoneUpdaterService.afterUpdate(host2);

    assertEquals(2, zoneUpdaterService.getCurrentHosts().size());
  }

  private HostInfoHolder createHost(String sld, String zone, String ipv4) throws UnknownHostException {
    HostInfoHolder host = new HostInfoHolder();
    host.setSld(sld);
    host.setZone(zone);
    host.setIpSetting(new IpSetting(InetAddress.getByName(ipv4), null));
    return host;
  }
}
