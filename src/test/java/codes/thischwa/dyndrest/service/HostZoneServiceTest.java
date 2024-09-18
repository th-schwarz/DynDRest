package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.Zone;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.jdbc.core.JdbcTemplate;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HostZoneServiceTest extends AbstractIntegrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private HostZoneService service;

  @Order(0)
  @Test
  void testGetZone() {
    Zone zone = service.getZone("dynhost1.info");
    assertNotNull(zone);

    zone = service.getZone("unknown.info");
    assertNull(zone);
  }

  @Order(1)
  @Test
  void testGetConfiguredHosts() {
    List<HostEnriched> hostEnricheds = service.getConfiguredHosts();
    assertEquals(4, hostEnricheds.size());
    HostEnriched host = hostEnricheds.get(0);
    assertEquals(1, host.getId());
    assertEquals("my0.dynhost0.info", host.getFullHost());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("ns0.domain.info", host.getNs());
  }

  @Order(2)
  @Test
  void testGetConfiguredZones() {
    List<Zone> zones = service.getConfiguredZones();
    assertEquals(2, zones.size());
    Zone zone = zones.get(0);
    assertEquals("dynhost0.info", zone.getName());
    assertEquals("ns0.domain.info", zone.getNs());
    zone = zones.get(1);
    assertEquals("dynhost1.info", zone.getName());
    assertEquals("ns1.domain.info", zone.getNs());
  }

  @Order(3)
  @Test
  void testHostExists() {
    assertTrue(service.hostExists("my0.dynhost0.info"));
    assertFalse(service.hostExists("unknown"));
  }

  @Order(4)
  @Test
  void testGetHost() {
    Optional<HostEnriched> optHost = service.getHost("my0.dynhost0.info");
    assertTrue(optHost.isPresent());
    HostEnriched host = optHost.get();
    assertEquals(1, host.getId());
    assertEquals("my0.dynhost0.info", host.getFullHost());
    assertEquals("ns0.domain.info", host.getNs());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("dynhost0.info", host.getZone());

    assertFalse(service.getHost("unknown").isPresent());
  }

  @Order(5)
  @Test
  void testValidate() {
    assertTrue(service.validate("my0.dynhost0.info", "1234567890abcdef"));
    assertFalse(service.validate("my0.dynhost0.info", "1234567890abcdefx"));

    assertThrows(EmptyResultDataAccessException.class, () -> service.validate("unknown", "token"));
  }

  @Order(6)
  @Test
  void testFindHostsOfZone() {
    Optional<List<HostEnriched>> optional = service.findHostsOfZone("dynhost0.info");
    assertFalse(optional.isEmpty());
    List<HostEnriched> hosts = optional.get();
    assertEquals(2, hosts.size());
    HostEnriched host = hosts.get(0);
    assertEquals(1, host.getId());
    assertEquals("my0.dynhost0.info", host.getFullHost());
    assertEquals("ns0.domain.info", host.getNs());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("dynhost0.info", host.getZone());

    optional = service.findHostsOfZone("unknown");
    assertTrue(optional.isEmpty());
  }

  @Order(7)
  @Test
  void testImportOnStart() {
    assertEquals(2, service.getConfiguredZones().size());
    assertEquals(4, service.getConfiguredHosts().size());
    service.importOnStart();
    assertEquals(3, service.getConfiguredZones().size());
    assertEquals(7, service.getConfiguredHosts().size());
  }

  @Order(8)
  @Test
  void testSaveUpdateHost() {
    int hostCnt = service.getConfiguredHosts().size();
    Host host = new Host();
    host.setName("my3");
    host.setApiToken("0987654321fedcba");
    host.setZoneId(2);
    service.saveOrUpdate(host);
    Integer id = host.getId();
    assertTrue(id != null && id > 4);
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

  @Order(9)
  @Test
  void testSaveFullHost() {
    HostEnriched hostEnriched = new HostEnriched();
    hostEnriched.setName("my4");
    hostEnriched.setApiToken("08/15");
    hostEnriched.setZoneId(2);
    service.saveOrUpdate(hostEnriched);
    assertTrue(hostEnriched.getId() != null && hostEnriched.getId() > 4);
    assertNotNull(hostEnriched.getChanged());

    hostEnriched.setId(null);
    try {
      service.saveOrUpdate(hostEnriched);
    } catch (Exception e) {
      assertEquals(DuplicateKeyException.class, e.getCause().getClass());
    }
  }

  @Order(10)
  @Test
  void testSaveZoneDuplicate() {
    Zone zone = new Zone();
    zone.setName("zone1.org");
    zone.setNs("ns1.zone.org");
    service.saveOrUpdate(zone);
    assertTrue(zone.getId() != null && zone.getId() > 2);
    assertNotNull(zone.getChanged());

    zone.setId(null);
    try {
      service.saveOrUpdate(zone);
    } catch (Exception e) {
      assertEquals(DuplicateKeyException.class, e.getCause().getClass());
    }
  }

  @Order(11)
  @Test
  void testAddZone() {
    Zone z = service.addZone("zone2.org", "ns2.zone.org");
    assertNotNull(z.getId());
    assertNotNull(z.getChanged());

    assertThrows(
        DbActionExecutionException.class, () -> service.addZone("zone2.org", "ns2.zone.org"));
  }

  @Order(12)
  @Test
  void testDeleteZone() {
    Optional<HostEnriched> h = service.getHost("test0.dynhost0.info");
    assertTrue(h.isPresent());
    Integer id = h.get().getId();
    Integer count =
        jdbcTemplate.queryForObject(
            "select count(*) from UPDATE_LOG where HOST_ID=?", Integer.class, id);
    assertTrue(count != null && count > 0);
    Zone zone = service.getZone("dynhost0.info");
    assertNotNull(zone);

    service.deleteZone(zone);
    assertNull(service.getZone("dynhost0.info"));
    h = service.getHost("test0.dynhost0.info");
    assertFalse(h.isPresent());
    count =
        jdbcTemplate.queryForObject(
            "select count(*) from UPDATE_LOG where HOST_ID=?", Integer.class, id);
    assertEquals(0, count);
  }

  @Order(13)
  @Test
  void testAddHost() {
    Zone z = service.getAllZones().get(0);
    int hostCnt = service.findHostsOfZone(z.getName()).orElse(new ArrayList<>()).size();
    Host host = new Host();
    host.setName("my5");
    host.setApiToken("abcdef1234567890");
    host.setZoneId(z.getId());
    service.saveOrUpdate(host);
    Optional<List<HostEnriched>> optHosts = service.findHostsOfZone(z.getName());
    assertTrue(optHosts.isPresent());
    List<HostEnriched> hosts = optHosts.get();
    assertEquals(hostCnt + 1, hosts.size());
    Optional<HostEnriched> optFullHost =
        hosts.stream().filter(h -> h.getName().startsWith("my5")).findFirst();
    assertTrue(optFullHost.isPresent());
    HostEnriched hostEnriched = optFullHost.get();
    assertNotNull(hostEnriched.getId());
    assertEquals(z.getId(), hostEnriched.getZoneId());
  }
}
