package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.Zone;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ZoneRepoTest extends AbstractIntegrationTest {

  @Autowired private ZoneRepo repo;

  @Test
  void testFindById() {
    Optional<Zone> zoneOpt = repo.findById(1);
    assertTrue(zoneOpt.isPresent());
    Zone zone = zoneOpt.get();
    assertEquals("dynhost0.info", zone.getName());
    assertEquals("ns0.domain.info", zone.getNs());
    assertEquals(currentDate, zone.getChanged().toLocalDate());
  }

  @Test
  void testFindByName() {
    Zone zone = repo.findByName("dynhost1.info");
    assertEquals(2, zone.getId());
    assertEquals("ns1.domain.info", zone.getNs());
    assertEquals(currentDate, zone.getChanged().toLocalDate());

    assertNull(repo.findByName("unknown"));
  }

  @Test
  void testUpdate() {
    Zone zone = new Zone();
    zone.setName("newzone2.info");
    zone.setNs("new2.ns.info");
    zone.setChanged(LocalDateTime.now());
    repo.save(zone);

    zone = repo.findByName("newzone2.info");
    assertEquals("new2.ns.info", zone.getNs());
    zone.setNs("ns2.new.info");
    repo.save(zone);

    zone = repo.findByName("newzone2.info");
    assertEquals("ns2.new.info", zone.getNs());

    repo.delete(zone);

    zone = repo.findByName("newzone2.info");
    assertNull(zone);
  }

  @Test
  void testSave() {
    Zone zone = new Zone();
    zone.setName("newzone1.info");
    zone.setNs("new1.ns.info");
    zone.setChanged(LocalDateTime.now());
    repo.save(zone);

    zone = repo.findByName("newzone1.info");
    assertEquals("new1.ns.info", zone.getNs());

    repo.delete(zone);
    zone = repo.findByName("newzone1.info");
    assertNull(zone);
  }
}
