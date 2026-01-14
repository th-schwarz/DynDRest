package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

  @BeforeEach
  void setUp() {
    updateLogService = mock(UpdateLogService.class);
    zoneUpdaterService = new ZoneUpdaterService(updateLogService);
  }

  @Test
  void testAddOrUpdateHost_NewHost() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdaterService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToCreate = zoneUpdaterService.getHostsPerZoneToCreate().get(EXAMPLE_ZONE);
    assertEquals(1, hostsToCreate.size());
    assertEquals("www.example.com", hostsToCreate.get(0).getFullHost());
    assertNull(zoneUpdaterService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE));
  }

  @Test
  void testAddOrUpdateHost_UpdateExistingHostWithDifferentIp() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("www", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdaterService.addOrUpdateHost(host1);
    zoneUpdaterService.deleteHost("www.example.com");
    zoneUpdaterService.afterUpdate(host1);
    zoneUpdaterService.addOrUpdateHost(host2);

    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
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

    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
    assertNull(hostsToUpdate);
  }

  @Test
  void testDeleteHost() {
    zoneUpdaterService.deleteHost("www.example.com");

    List<String> hostsToDelete = zoneUpdaterService.getHostsPerZoneToDelete().get(EXAMPLE_ZONE);
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
