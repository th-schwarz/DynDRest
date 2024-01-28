package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HostZoneServiceTest extends GenericIntegrationTest {

  @Autowired private HostZoneService service;

  @Test
  void testGetConfigured() {
    List<FullHost> hosts = service.getConfiguredHosts();
    assertEquals(4, hosts.size());
    assertEquals("dynhost0.info", hosts.get(0).getZone());
  }

  @Test
  void testHostExists() {
    assertTrue(service.hostExists("my0.dynhost0.info"));
    assertFalse(service.hostExists("unknown"));
  }
}
