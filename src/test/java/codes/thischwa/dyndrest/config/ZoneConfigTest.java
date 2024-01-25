package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.service.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZoneConfigTest extends GenericIntegrationTest {

  private final int configuredEntries = 2;

  @Autowired private ZoneConfig zoneConfig;

  @Autowired private ZoneService zoneService;

  @Test
  final void testCountZones() {
    assertEquals(configuredEntries, zoneConfig.zones().size());
  }

  @Test
  final void testZoneDetails() {
    Zone zone = zoneConfig.zones().get(0);
    assertEquals("dynhost0.info", zone.name());
    assertEquals("ns0.domain.info", zone.ns());

    assertEquals("my0:1234567890abcdef", zone.hosts().get(0));
    assertEquals("test0:1234567890abcdx", zone.hosts().get(1));
  }
}
