package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class ZoneUpdateOrderServiceTest {

  static final String EXAMPLE_ZONE = "example.com";
  private ZoneUpdateOrderService zoneUpdateOrderService;
  private UpdateLogService updateLogService;

  @BeforeEach
  void setUp() {
    updateLogService = mock(UpdateLogService.class);
    zoneUpdateOrderService = new ZoneUpdateOrderService(updateLogService);
  }

  @Test
  void testAddOrUpdateHost_NewHost() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdateOrderService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToUpdate = zoneUpdateOrderService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
    assertEquals(1, hostsToUpdate.size());
    assertEquals("www.example.com", hostsToUpdate.get(0).getFullHost());
  }

  @Test
  void testAddOrUpdateHost_UpdateExistingHostWithDifferentIp() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("www", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdateOrderService.addOrUpdateHost(host1);
    zoneUpdateOrderService.deleteHost("www.example.com");
    zoneUpdateOrderService.afterUpdate(host1);
    zoneUpdateOrderService.addOrUpdateHost(host2);

    List<HostInfoHolder> hostsToUpdate = zoneUpdateOrderService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
    assertEquals(1, hostsToUpdate.size());
    assertEquals("192.168.1.2", hostsToUpdate.get(0).getIpSetting().getIpv4().getHostAddress());
  }

  @Test
  void testAddOrUpdateHost_SameIpNoUpdate() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdateOrderService.addOrUpdateHost(host);
    zoneUpdateOrderService.deleteHost("www.example.com");
    zoneUpdateOrderService.afterUpdate(host);
    zoneUpdateOrderService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToUpdate = zoneUpdateOrderService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
    assertTrue(hostsToUpdate.isEmpty());
  }

  @Test
  void testDeleteHost() {
    zoneUpdateOrderService.deleteHost("www.example.com");

    List<String> hostsToDelete = zoneUpdateOrderService.getHostsPerZoneToDelete().get(EXAMPLE_ZONE);
    assertEquals(1, hostsToDelete.size());
    assertEquals("www.example.com", hostsToDelete.get(0));
  }

  @Test
  void testAfterUpdate() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdateOrderService.addOrUpdateHost(host);
    zoneUpdateOrderService.deleteHost("www.example.com");
    zoneUpdateOrderService.afterUpdate(host);

    assertTrue(zoneUpdateOrderService.hostExists("www.example.com"));
    verify(updateLogService, times(1)).log(eq("www.example.com"), any(IpSetting.class), eq(UpdateLog.Status.success));
  }

  @Test
  void testHostExists() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    assertFalse(zoneUpdateOrderService.hostExists("www.example.com"));

    zoneUpdateOrderService.addOrUpdateHost(host);
    zoneUpdateOrderService.deleteHost("www.example.com");
    zoneUpdateOrderService.afterUpdate(host);

    assertTrue(zoneUpdateOrderService.hostExists("www.example.com"));
  }

  @Test
  void testRemoveFromCurrentHosts() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    zoneUpdateOrderService.addOrUpdateHost(host);
    zoneUpdateOrderService.deleteHost("www.example.com");
    zoneUpdateOrderService.afterUpdate(host);
    assertTrue(zoneUpdateOrderService.hostExists("www.example.com"));

    zoneUpdateOrderService.removeFromCurrentHosts("www.example.com");
    assertFalse(zoneUpdateOrderService.hostExists("www.example.com"));
  }

  @Test
  void testGetCurrentHosts() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("mail", EXAMPLE_ZONE, "192.168.1.2");

    zoneUpdateOrderService.addOrUpdateHost(host1);
    zoneUpdateOrderService.deleteHost("www.example.com");
    zoneUpdateOrderService.afterUpdate(host1);
    zoneUpdateOrderService.addOrUpdateHost(host2);
    zoneUpdateOrderService.deleteHost("mail.example.com");
    zoneUpdateOrderService.afterUpdate(host2);

    assertEquals(2, zoneUpdateOrderService.getCurrentHosts().size());
  }

  private HostInfoHolder createHost(String sld, String zone, String ipv4) throws UnknownHostException {
    HostInfoHolder host = new HostInfoHolder();
    host.setSld(sld);
    host.setZone(zone);
    host.setIpSetting(new IpSetting(InetAddress.getByName(ipv4), null));
    return host;
  }
}
