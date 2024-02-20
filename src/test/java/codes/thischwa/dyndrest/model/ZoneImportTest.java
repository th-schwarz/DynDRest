package codes.thischwa.dyndrest.model;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ZoneImportTest extends AbstractIntegrationTest {

  @Autowired private ZoneImport zoneImport;

  @Test
  final void testConfig() {
    FullHost fullHost = zoneImport.getHosts().get(0);
    assertNull(fullHost.getId());
    assertNull(fullHost.getZoneId());
    assertNull(fullHost.getChanged());
    assertEquals("my3.dynhost0.info", fullHost.getFullHost());
    assertEquals("dynhost0.info", fullHost.getZone());
    assertEquals("ns0.domain.info", fullHost.getNs());
    assertEquals("api1", fullHost.getApiToken());
  }
}
