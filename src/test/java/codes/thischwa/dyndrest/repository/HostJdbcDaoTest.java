package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

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
    assertEquals(1, host.getZoneId());
  }

  @Test
  void testGetAllExtended() {
    List<FullHost> hosts = repo.getAllExtended();
    assertEquals(4, hosts.size());

    FullHost host = hosts.get(0);
    assertEquals(1, host.getId());
    assertEquals("my0", host.getName());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals("ns0.domain.info", host.getNs());
    assertEquals("dynhost0.info", host.getZone());
    assertNotNull(host.getChanged());
  }

  @Test
  void testGetByFullHost() {
    Host host = repo.getByFullHost("my1.dynhost1.info");
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(2, host.getZoneId());

    assertThrows(EmptyResultDataAccessException.class, () -> repo.getByFullHost("unknown"));
  }
}
