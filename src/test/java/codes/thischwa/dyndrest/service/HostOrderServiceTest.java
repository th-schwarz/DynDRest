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

class HostOrderServiceTest {

  static final String EXAMPLE_ZONE = "example.com";
  private HostOrderService hostOrderService;
  private UpdateLogService updateLogService;

  @BeforeEach
  void setUp() {
    updateLogService = mock(UpdateLogService.class);
    hostOrderService = new HostOrderService(updateLogService);
  }

  @Test
  void testAddOrUpdateHost_NewHost() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    hostOrderService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToUpdate = hostOrderService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
    assertEquals(1, hostsToUpdate.size());
    assertEquals("www.example.com", hostsToUpdate.get(0).getFullHost());
  }

  @Test
  void testAddOrUpdateHost_UpdateExistingHostWithDifferentIp() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("www", EXAMPLE_ZONE, "192.168.1.2");

    hostOrderService.addOrUpdateHost(host1);
    hostOrderService.deleteHost("www.example.com");
    hostOrderService.afterUpdate(host1);
    hostOrderService.addOrUpdateHost(host2);

    List<HostInfoHolder> hostsToUpdate = hostOrderService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
    assertEquals(1, hostsToUpdate.size());
    assertEquals("192.168.1.2", hostsToUpdate.get(0).getIpSetting().getIpv4().getHostAddress());
  }

  @Test
  void testAddOrUpdateHost_SameIpNoUpdate() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    hostOrderService.addOrUpdateHost(host);
    hostOrderService.deleteHost("www.example.com");
    hostOrderService.afterUpdate(host);
    hostOrderService.addOrUpdateHost(host);

    List<HostInfoHolder> hostsToUpdate = hostOrderService.getHostsPerZoneToUpdate().get(EXAMPLE_ZONE);
    assertTrue(hostsToUpdate.isEmpty());
  }

  @Test
  void testDeleteHost() {
    hostOrderService.deleteHost("www.example.com");

    List<String> hostsToDelete = hostOrderService.getHostsPerZoneToDelete().get(EXAMPLE_ZONE);
    assertEquals(1, hostsToDelete.size());
    assertEquals("www.example.com", hostsToDelete.get(0));
  }

  @Test
  void testAfterUpdate() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    hostOrderService.addOrUpdateHost(host);
    hostOrderService.deleteHost("www.example.com");
    hostOrderService.afterUpdate(host);

    assertTrue(hostOrderService.hostExists("www.example.com"));
    verify(updateLogService, times(1)).log(eq("www.example.com"), any(IpSetting.class), eq(UpdateLog.Status.success));
  }

  @Test
  void testHostExists() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    assertFalse(hostOrderService.hostExists("www.example.com"));

    hostOrderService.addOrUpdateHost(host);
    hostOrderService.deleteHost("www.example.com");
    hostOrderService.afterUpdate(host);

    assertTrue(hostOrderService.hostExists("www.example.com"));
  }

  @Test
  void testRemoveFromCurrentHosts() throws UnknownHostException {
    HostInfoHolder host = createHost("www", EXAMPLE_ZONE, "192.168.1.1");

    hostOrderService.addOrUpdateHost(host);
    hostOrderService.deleteHost("www.example.com");
    hostOrderService.afterUpdate(host);
    assertTrue(hostOrderService.hostExists("www.example.com"));

    hostOrderService.removeFromCurrentHosts("www.example.com");
    assertFalse(hostOrderService.hostExists("www.example.com"));
  }

  @Test
  void testGetCurrentHosts() throws UnknownHostException {
    HostInfoHolder host1 = createHost("www", EXAMPLE_ZONE, "192.168.1.1");
    HostInfoHolder host2 = createHost("mail", EXAMPLE_ZONE, "192.168.1.2");

    hostOrderService.addOrUpdateHost(host1);
    hostOrderService.deleteHost("www.example.com");
    hostOrderService.afterUpdate(host1);
    hostOrderService.addOrUpdateHost(host2);
    hostOrderService.deleteHost("mail.example.com");
    hostOrderService.afterUpdate(host2);

    assertEquals(2, hostOrderService.getCurrentHosts().size());
  }

  private HostInfoHolder createHost(String sld, String zone, String ipv4) throws UnknownHostException {
    HostInfoHolder host = new HostInfoHolder();
    host.setSld(sld);
    host.setZone(zone);
    host.setIpSetting(new IpSetting(InetAddress.getByName(ipv4), null));
    return host;
  }
}
