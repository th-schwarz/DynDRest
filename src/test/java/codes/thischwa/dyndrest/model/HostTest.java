package codes.thischwa.dyndrest.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HostTest {

  @Test
  void testGetFullHost() {
    FullHost fullHost = new FullHost();
    fullHost.setName("my4");
    fullHost.setApiToken("08/15");
    fullHost.setZoneId(2);
    fullHost.setZone("zone.info");
    assertEquals("my4.zone.info", fullHost.getFullHost());
  }
}
