package codes.thischwa.dyndrest.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HostTest {

  @Test
  void testGetFullHost() {
    HostEnriched hostEnriched = new HostEnriched();
    hostEnriched.setName("my4");
    hostEnriched.setApiToken("08/15");
    hostEnriched.setZoneId(2);
    hostEnriched.setZone("zone.info");
    assertEquals("my4.zone.info", hostEnriched.getFullHost());
  }
}
