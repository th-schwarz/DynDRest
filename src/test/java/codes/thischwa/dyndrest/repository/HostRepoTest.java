package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.Host;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HostRepoTest extends AbstractIntegrationTest {

  @Autowired private HostRepo repo;

  @Test
  void testGetAll() {
    List<HostEnriched> hosts = repo.findAllExtended();
    assertEquals(4, hosts.size());

    Host host = hosts.get(0);
    assertEquals(1, host.getId());
    assertEquals("my0", host.getName());
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(1, host.getZoneId());
    assertEquals("2024-01-28T12:06:29.821934", host.getChanged().toString());
  }

  @Test
  void testGetByFullHost() {
    Optional<HostEnriched> optFullHost = repo.findByFullHost("my1.dynhost1.info");
    assertTrue(optFullHost.isPresent());
    HostEnriched host = optFullHost.get();
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(2, host.getZoneId());
    assertEquals("dynhost1.info", host.getZone());
    assertEquals("ns1.domain.info", host.getNs());

    assertFalse(repo.findByFullHost("unknown").isPresent());
  }

  @Test
  void testFindHostsOfZone() {
    List<HostEnriched> hosts = repo.findByZoneId(1);
    assertEquals(2, hosts.size());

    HostEnriched host1 = hosts.get(0);
    assertEquals(1, host1.getId());
    assertEquals("my0", host1.getName());
    assertEquals("1234567890abcdef", host1.getApiToken());
    assertEquals(1, host1.getZoneId());
    assertEquals("2024-01-28T12:06:29.821934", host1.getChanged().toString());

    HostEnriched host2 = hosts.get(1);
    assertEquals(2, host2.getId());
    assertEquals("test0", host2.getName());

    hosts = repo.findByZoneId(1000);
    assertTrue(hosts.isEmpty());
  }
}
