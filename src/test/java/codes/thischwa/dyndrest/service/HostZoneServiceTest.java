package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

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

  @Test
  void testGetHost() {
    FullHost host = service.getHost("my0.dynhost0.info");
    assertEquals(1, host.getId());
    assertEquals("my0.dynhost0.info", host.getFullHost());
    assertEquals("ns0.domain.info", host.getNs());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("dynhost0.info", host.getZone());

    assertThrows(EmptyResultDataAccessException.class, () -> service.getHost("unknown"));
  }

  @Test
  void testValidate() {
    assertTrue(service.validate("my0.dynhost0.info", "1234567890abcdef"));
    assertFalse(service.validate("my0.dynhost0.info", "1234567890abcdefx"));

    assertThrows(EmptyResultDataAccessException.class, () -> service.validate("unknown", "token"));
  }
}
