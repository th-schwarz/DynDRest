package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.service.HostZoneService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConstraintTest extends AbstractIntegrationTest {

  @Autowired private HostZoneService service;
  @Autowired private ZoneRepo zoneRepo;
  @Autowired private UpdateLogRepo logRepo;

  @Test
  void testDuplicateZone() {
    Zone zone = new Zone();
    zone.setName("newzone1.info");
    zone.setNs("new1.ns.info");
    zone.setChanged(LocalDateTime.now());
    zoneRepo.save(zone);

    Zone duplicateZone = new Zone();
    duplicateZone.setName("newzone1.info");
    duplicateZone.setNs("new1.ns.info");
    duplicateZone.setChanged(LocalDateTime.now());
    try {
      service.saveOrUpdate(zone);
    } catch (DbActionExecutionException e) {
      assertEquals(e.getCause().getClass(), DuplicateKeyException.class);
    }
  }

  @Test
  void testDuplicateHost() {
    Host host = new Host();
    host.setName("newhost1");
    host.setApiToken("token1");
    host.setZoneId(1);
    host.setChanged(LocalDateTime.now());
    service.saveOrUpdate(host);
    Host duplicateHost = new Host();
    duplicateHost.setName("newhost1");
    duplicateHost.setApiToken("token1");
    duplicateHost.setZoneId(1);
    duplicateHost.setChanged(LocalDateTime.now());
    try {
      service.saveOrUpdate(duplicateHost);
    } catch (DbActionExecutionException e) {
      assertEquals(e.getCause().getClass(), DuplicateKeyException.class);
    }
  }

  @Test
  @Order(Integer.MAX_VALUE)
  void testCascadeDelete() {
    Zone zone = zoneRepo.findByName("dynhost0.info");
    Optional<List<FullHost>> fullHosts = service.findHostsOfZone(zone.getName());
    assertFalse(fullHosts.isEmpty());
    Integer hostId = fullHosts.get().get(0).getId();
    assertFalse(logRepo.findByHostId(hostId).isEmpty());

    service.deleteZone(zone);
    assertTrue(service.findHostsOfZone(zone.getName()).isEmpty());

    assertTrue(logRepo.findByHostId(hostId).isEmpty());
  }
}
