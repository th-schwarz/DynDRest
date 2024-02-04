package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

class HostJdbcDaoTest extends AbstractIntegrationTest {

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
    assertEquals("2024-01-28T12:06:29.821934", host.getChanged().toString());
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

  @Test
  void testHashAndEquals() {
    FullHost fullHost1 = new FullHost();
    fullHost1.setId(1);
    fullHost1.setName("my0");
    fullHost1.setApiToken("1234567890abcdef");
    fullHost1.setNs("ns0.domain.info");
    fullHost1.setZoneId(1);
    fullHost1.setZone("dynhost0.info");
    fullHost1.setChanged(LocalDateTime.parse("2024-01-28T12:06:29.821934"));

    FullHost fullHost2 = new FullHost();
    fullHost2.setId(1);
    fullHost2.setName("my0");
    fullHost2.setApiToken("1234567890abcdef");
    fullHost2.setNs("ns0.domain.info");
    fullHost2.setZoneId(1);
    fullHost2.setZone("dynhost0.info");
    fullHost2.setChanged(LocalDateTime.parse("2024-01-28T12:06:29.821934"));

    FullHost fullHost3 = new FullHost();
    fullHost3.setId(2);
    fullHost3.setName("my1");
    fullHost3.setApiToken("1234567890abcdef");
    fullHost3.setNs("ns1.domain.info");
    fullHost3.setZoneId(2);
    fullHost3.setZone("dynhost1.info");
    fullHost3.setChanged(LocalDateTime.parse("2024-01-28T12:06:29.821934"));

    assertEquals(fullHost1, fullHost2);
    assertNotEquals(fullHost1, fullHost3);

    Host host1 = new Host();
    host1.setId(1);
    host1.setName("my0");
    host1.setZoneId(1);
    host1.setApiToken("1234567890abcdef");
    fullHost1.setChanged(LocalDateTime.parse("2024-01-28T12:06:29.821934"));
    assertEquals(host1.hashCode(), fullHost1.hashCode());
  }
}
