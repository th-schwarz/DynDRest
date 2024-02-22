package codes.thischwa.dyndrest.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HostTest {

  @Test
  void testGetInstance() {
    String name = "my";
    String apiToken = "xyz";
    Integer zoneId = 1;
    LocalDateTime changed = LocalDateTime.now();

    Host host = new Host(name, apiToken, zoneId, changed);
    assertEquals(name, host.getName());
    assertEquals(apiToken, host.getApiToken());
    assertEquals(zoneId, host.getZoneId());
    assertEquals(changed, host.getChanged());
  }

  @Test
  void testGetFullHost() {
    FullHost host = new FullHost();
    host.setName("my");
    host.setZone("zone.info");

    String expectedHost = "my.zone.info";
    assertEquals(expectedHost, host.getFullHost());
  }
}