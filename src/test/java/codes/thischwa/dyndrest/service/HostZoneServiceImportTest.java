package codes.thischwa.dyndrest.service;


import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HostZoneServiceImportTest extends AbstractIntegrationTest {

  @Autowired private HostZoneService service;

  @Test
  void testImportOnStart() {
    assertEquals(2, service.getConfiguredZones().size());
    assertEquals(4, service.getConfiguredHosts().size());
    service.importOnStart();
    assertEquals(3, service.getConfiguredZones().size());
    assertEquals(7, service.getConfiguredHosts().size());
  }
}
