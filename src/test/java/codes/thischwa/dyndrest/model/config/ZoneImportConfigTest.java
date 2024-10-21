package codes.thischwa.dyndrest.model.config;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.HostEnriched;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ZoneImportConfigTest extends AbstractIntegrationTest {

  @Autowired private ZoneImportConfig zoneImportConfig;

  @Test
  final void testConfig() {
    HostEnriched hostEnriched = zoneImportConfig.getHosts().get(0);
    assertNull(hostEnriched.getId());
    assertNull(hostEnriched.getZoneId());
    assertNull(hostEnriched.getChanged());
    assertEquals("my3.dynhost0.info", hostEnriched.getFullHost());
    assertEquals("dynhost0.info", hostEnriched.getZone());
    assertEquals("ns0.domain.info", hostEnriched.getNs());
    assertEquals("api1", hostEnriched.getApiToken());
  }
}
