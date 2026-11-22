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
  void testAddOrUpdateHost_InitialAssignmentIsRejected() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    assertThrows(IllegalStateException.class, () -> zoneUpdaterService.addOrUpdateHost(host));
    assertTrue(zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE).isEmpty());
  }

  @Test
  void testIsInitialAssignment() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    assertTrue(zoneUpdaterService.isInitialAssignment(host.getFullHost()));

    zoneUpdaterService.addCurrentHosts(host);
    assertFalse(zoneUpdaterService.isInitialAssignment(host.getFullHost()));

    HostInfoHolder withoutIp = createHost("empty", EXAMPLE_ZONE, "192.168.1.1");
    withoutIp.setIpSetting(new IpSetting((InetAddress) null, null));
    zoneUpdaterService.addCurrentHosts(withoutIp);
    assertTrue(zoneUpdaterService.isInitialAssignment(withoutIp.getFullHost()));

    when(hostZoneService.hostExists("deleted.example.com")).thenReturn(false);
    assertThrows(IllegalArgumentException.class, () -> zoneUpdaterService.isInitialAssignment("deleted.example.com"));
  }

  @Test
  void testAddOrUpdateHost_UpdateExistingHostWithDifferentIp() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("www", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdaterService.addCurrentHosts(host1);
    zoneUpdaterService.addOrUpdateHost(host2);

    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE);
    assertEquals(1, hostsToUpdate.size());
    assertEquals("192.168.1.2", hostsToUpdate.get(0).getIpSetting().getIpv4().getHostAddress());
  }

  @Test
  void testAddOrUpdateHost_SameIpNoUpdate() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdaterService.addCurrentHosts(host);
    zoneUpdaterService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE);
    assertTrue(hostsToUpdate.isEmpty());
  }

  @Test
  void testAfterUpdate() throws UnknownHostException {
    HostInfoHolder current = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder update = createHost("www", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdaterService.addCurrentHosts(current);
    zoneUpdaterService.addOrUpdateHost(update);
    zoneUpdaterService.afterUpdate(update);

    assertTrue(zoneUpdaterService.hostExists("www.example.com"));
    verify(updateLogService, times(1)).log(eq("www.example.com"), any(IpSetting.class), eq(UpdateLog.Status.success));
    assertTrue(zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE).isEmpty());
  }

  @Test
  void testAddOrUpdateHost_UnknownHost() throws UnknownHostException {
    HostInfoHolder host = createHost("deleted", EXAMPLE_ZONE, "192.168.1.1");
    when(hostZoneService.hostExists(host.getFullHost())).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> zoneUpdaterService.addOrUpdateHost(host));
    assertTrue(zoneUpdaterService.getHostsToUpdate(EXAMPLE_ZONE).isEmpty());
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

    zoneUpdaterService.addCurrentHosts(host);

    assertTrue(zoneUpdaterService.hostExists("www.example.com"));
  }

  @Test
  void testRemoveFromCurrentHosts() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdaterService.addCurrentHosts(host);

    zoneUpdaterService.removeFromCurrentHosts("www.example.com");
    assertFalse(zoneUpdaterService.hostExists("www.example.com"));
  }

  @Test
  void testGetCurrentHosts() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("mail", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdaterService.addCurrentHosts(host1, host2);

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
