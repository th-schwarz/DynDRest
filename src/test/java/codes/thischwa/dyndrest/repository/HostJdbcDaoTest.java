package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.Host;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class HostJdbcDaoTest extends GenericIntegrationTest {

  @Autowired private HostJdbcDao repo;

  @Test
  void testGetAll() {
    List<Host> hosts = repo.getAll();
    assertEquals(4, hosts.size());

    Host host = hosts.get(0);
    assertEquals(1, host.getId());
    assertEquals("my0", host.getName());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertNull(host.getNs());
    assertNull(host.getZone());
  }

  @Test
  void testGetAllExtended() {
    List<Host> hosts = repo.getAllExtended();
    assertEquals(4, hosts.size());

    Host host = hosts.get(0);
    assertEquals(1, host.getId());
    assertEquals("my0", host.getName());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals("ns0.domain.info", host.getNs());
    assertEquals("dynhost0.info", host.getZone());
  }
}
