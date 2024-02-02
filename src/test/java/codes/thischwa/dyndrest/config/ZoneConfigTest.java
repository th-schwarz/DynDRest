package codes.thischwa.dyndrest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.service.HostZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ZoneConfigTest extends AbstractIntegrationTest {

  private final int configuredEntries = 2;

  @Autowired private ZoneConfig zoneConfig;

  @Autowired private HostZoneService hostZoneService;

  @Test
  final void testCountZones() {
    assertEquals(configuredEntries, zoneConfig.zones().size());
  }

  @Test
  final void testZoneDetails() {
    Zone zone = zoneConfig.zones().get(0);
    assertEquals("dynhost0.info", zone.getName());
    assertEquals("ns0.domain.info", zone.getNs());
  }
}
