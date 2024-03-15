package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.Zone;
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
    assertEquals("2024-01-28T12:00:37.013707", zone.getChanged().toString());
  }

  @Test
  void testFindByName() {
    Zone zone = repo.findByName("dynhost1.info");
    assertEquals(2, zone.getId());
    assertEquals("ns1.domain.info", zone.getNs());
    assertEquals("2024-01-28T12:00:37.013707", zone.getChanged().toString());

    assertNull(repo.findByName("unknown"));
  }
}
