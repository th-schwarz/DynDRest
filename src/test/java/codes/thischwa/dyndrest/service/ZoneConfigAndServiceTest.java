package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ZoneConfigAndServiceTest extends AbstractIntegrationTest {

  private final int configuredEntries = 2;

  @Autowired private HostZoneService hostZoneService;

  @Test
  void testGetPrimaryNameServer() {
    assertEquals("ns1.domain.info", hostZoneService.getHost("my1.dynhost1.info").get().getNs());
  }

  @Test
  final void testConfigured() {
    assertEquals(configuredEntries * 2, hostZoneService.getConfiguredHosts().size());
  }
}
