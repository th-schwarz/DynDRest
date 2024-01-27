package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.config.ZoneConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

class ZoneConfigAndServiceTest extends GenericIntegrationTest {

  private final int configuredEntries = 2;

  @Autowired private HostZoneService hostZoneService;

  @Autowired private ZoneConfig zoneConfig;


  @Test
  final void testGetPrimaryNameServer() {
    assertEquals("ns1.domain.info", hostZoneService.getPrimaryNameServer("dynhost1.info"));
    assertThrows(
        EmptyResultDataAccessException.class,
        () -> hostZoneService.getPrimaryNameServer("unknown-host.info"));
  }

  @Test
  final void testConfigured() {
    assertEquals(configuredEntries * 2, hostZoneService.getConfiguredHosts().size());
  }
}
