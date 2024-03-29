package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.Zone;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.Rollback;

@Rollback
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
    Optional<FullHost> optHost = service.getHost("my0.dynhost0.info");
    assertTrue(optHost.isPresent());
    FullHost host = optHost.get();
    assertEquals(1, host.getId());
    assertEquals("my0.dynhost0.info", host.getFullHost());
    assertEquals("ns0.domain.info", host.getNs());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("dynhost0.info", host.getZone());

    assertFalse(service.getHost("unknown").isPresent());
  }

  @Test
  void testSaveUpdateHost() {
    int hostCnt = service.getConfiguredHosts().size();
    Host host = new Host();
    host.setName("my3");
    host.setApiToken("0987654321fedcba");
    host.setZoneId(2);
    service.saveOrUpdate(host);
    Integer id = host.getId();
    assertTrue(id > 4);
    assertNotNull(host.getChanged());
    assertEquals(hostCnt + 1, service.getConfiguredHosts().size());

    host.setId(null);
    try {
      service.saveOrUpdate(host);
    } catch (Exception e) {
      assertEquals(DuplicateKeyException.class, e.getCause().getClass());
    }

    LocalDateTime oldChanged = host.getChanged();
    Optional<Host> optHost = service.findHostById(id);
    assertTrue(optHost.isPresent());
    host = optHost.get();
    host.setApiToken("-+");
    service.saveOrUpdate(host);

    optHost = service.findHostById(id);
    assertTrue(optHost.isPresent());
    host = optHost.get();
    assertEquals("-+", host.getApiToken());
    assertTrue(oldChanged.isBefore(host.getChanged()));
  }

  @Test
  void testSaveFullHost() {
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
  void testSaveZone() {
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
  void testValidate() {
    assertTrue(service.validate("my0.dynhost0.info", "1234567890abcdef"));
    assertFalse(service.validate("my0.dynhost0.info", "1234567890abcdefx"));

    assertThrows(EmptyResultDataAccessException.class, () -> service.validate("unknown", "token"));
  }

  @Test
  void testImportOnStart() {
    assertEquals(2, service.getConfiguredZones().size());
    assertEquals(4, service.getConfiguredHosts().size());
    service.importOnStart();
    assertEquals(3, service.getConfiguredZones().size());
    assertEquals(7, service.getConfiguredHosts().size());
  }
}
