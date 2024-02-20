package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.Zone;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

class HostZoneServiceTest extends AbstractIntegrationTest {

  @Autowired private HostZoneService service;

  @Test
  void testGetConfiguredHosts() {
    List<FullHost> fullHosts = service.getConfiguredHosts();
    assertEquals(4, fullHosts.size());
    FullHost host = fullHosts.get(0);
    assertEquals(1, host.getId());
    assertEquals("my0.dynhost0.info", host.getFullHost());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("ns0.domain.info", host.getNs());
  }

  @Test
  void testGetConfiguredZones() {
    List<Zone> zones = service.getConfiguredZones();
    assertEquals(2, zones.size());
    Zone zone = zones.get(1);
    assertEquals("dynhost1.info", zone.getName());
    assertEquals("ns1.domain.info", zone.getNs());
  }

  @Test
  void testHostExists() {
    assertTrue(service.hostExists("my0.dynhost0.info"));
    assertFalse(service.hostExists("unknown"));
  }

  @Test
  void testGetHost() {
    FullHost host = service.getHost("my0.dynhost0.info");
    assertEquals(1, host.getId());
    assertEquals("my0.dynhost0.info", host.getFullHost());
    assertEquals("ns0.domain.info", host.getNs());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("dynhost0.info", host.getZone());

    assertNull(service.getHost("unknown"));
  }

  @Test
  final void testSaveHost() {
    Host host = new Host();
    host.setName("my3");
    host.setApiToken("0987654321fedcba");
    host.setZoneId(2);
    service.saveOrUpdate(host);
    assertTrue(host.getId() > 4);
    assertNotNull(host.getChanged());

    host.setId(null);
    try {
      service.saveOrUpdate(host);
    } catch (Exception e) {
      assertEquals(DuplicateKeyException.class, e.getCause().getClass());
    }
  }

  @Test
  final void testSaveFullHost() {
    FullHost fullHost = new FullHost();
    fullHost.setName("my4");
    fullHost.setApiToken("08/15");
    fullHost.setZoneId(2);
    service.saveOrUpdate(fullHost);
    assertTrue(fullHost.getId() > 4);
    assertNotNull(fullHost.getChanged());

    fullHost.setId(null);
    try {
      service.saveOrUpdate(fullHost);
    } catch (Exception e) {
      assertEquals(DuplicateKeyException.class, e.getCause().getClass());
    }
  }

  @Test
  final void testSaveZone() {
    Zone zone = new Zone();
    zone.setName("zone1.org");
    zone.setNs("ns1.zone.org");
    service.saveOrUpdate(zone);
    assertTrue(zone.getId() > 2);
    assertNotNull(zone.getChanged());

    zone.setId(null);
    try {
      service.saveOrUpdate(zone);
    } catch (Exception e) {
      assertEquals(DuplicateKeyException.class, e.getCause().getClass());
    }
  }

  @Test
  final void testValidate() {
    assertTrue(service.validate("my0.dynhost0.info", "1234567890abcdef"));
    assertFalse(service.validate("my0.dynhost0.info", "1234567890abcdefx"));

    assertThrows(NullPointerException.class, () -> service.validate("unknown", "token"));
  }
}
