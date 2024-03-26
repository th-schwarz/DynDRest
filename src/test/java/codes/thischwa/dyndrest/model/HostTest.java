package codes.thischwa.dyndrest.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
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

  @Test
  void testEquals() {
    Host h1 = new Host();
    h1.setName("test1");
    h1.setZoneId(1);
    Host h2 = new Host();
    h2.setName("test1");
    h2.setZoneId(1);
    assertEquals(h1, h2);

    h1.setId(1);
    assertNotEquals(h1, h2);
    h2.setId(1);
    assertEquals(h1, h2);
    
    h1.setChanged(LocalDateTime.now());
    assertNotEquals(h1, h2);

    Host h3 = new Host();
    h3.setName("test2");
    h3.setZoneId(2);
    assertNotEquals(h1, h3);
  }
}
