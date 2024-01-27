package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HostZoneServiceTest extends GenericIntegrationTest {

  @Autowired private HostZoneService service;

  @Test
  void testGetConfigured() {
    List<String> hosts = service.getConfiguredHosts();
    assertEquals(4, hosts.size());
    assertEquals("my0.dynhost0.info", hosts.get(0));
  }

  @Test
  void testHostExists() {
    assertTrue(service.hostExists("my0.dynhost0.info"));
    assertFalse(service.hostExists("unknown"));
  }

  @Test
  void testGetPrimaryNameServer() {
    assertEquals("ns1.domain.info", service.getPrimaryNameServer("dynhost1.info"));
    assertThrows(EmptyResultDataAccessException.class, () ->  service.getPrimaryNameServer("unknown"));
  }
}
