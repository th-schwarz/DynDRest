package codes.thischwa.dyndrest.model;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ZoneImportTest extends AbstractIntegrationTest {

  @Autowired private ZoneImport zoneImport;

  @Test
  final void testConfig() {
    HostEnriched hostEnriched = zoneImport.getHosts().get(0);
    assertNull(hostEnriched.getId());
    assertNull(hostEnriched.getZoneId());
    assertNull(hostEnriched.getChanged());
    assertEquals("my3.dynhost0.info", hostEnriched.getFullHost());
    assertEquals("dynhost0.info", hostEnriched.getZone());
    assertEquals("ns0.domain.info", hostEnriched.getNs());
    assertEquals("api1", hostEnriched.getApiToken());
  }
}
