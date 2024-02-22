package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HostRepoTest extends AbstractIntegrationTest {

  @Autowired private HostRepo repo;

  @Test
  void testGetAll() {
    List<FullHost> hosts = repo.findAllExtended();
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
    Optional<FullHost> optFullHost = repo.findByFullHost("my1.dynhost1.info");
    assertTrue(optFullHost.isPresent());
    FullHost host = optFullHost.get();
    assertEquals("1234567890abcdef", host.getApiToken());
    assertEquals(2, host.getZoneId());
    assertEquals("dynhost1.info", host.getZone());
    assertEquals("ns1.domain.info", host.getNs());

    assertFalse(repo.findByFullHost("unknown").isPresent());
  }
}
