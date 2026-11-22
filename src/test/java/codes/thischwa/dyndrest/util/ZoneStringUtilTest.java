package codes.thischwa.dyndrest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.model.Zone;
import org.junit.jupiter.api.Test;

class ZoneStringUtilTest {

  @Test
  void testGetFqdn() {
    Zone zone = new Zone();
    zone.setName("example.com");

    String result = ZoneStringUtil.getFqdn("www", zone);
    assertEquals("www.example.com", result);
  }

  @Test
  void testGetFqdn_SubdomainWithDashes() {
    Zone zone = new Zone();
    zone.setName("example.com");

    String result = ZoneStringUtil.getFqdn("my-server", zone);
    assertEquals("my-server.example.com", result);
  }

  @Test
  void testGetZoneName() {
    String result = ZoneStringUtil.getZoneName("www.example.com");
    assertEquals("example.com", result);
  }

  @Test
  void testGetZoneName_MultiLevelSubdomain() {
    String result = ZoneStringUtil.getZoneName("mail.subdomain.example.com");
    assertEquals("subdomain.example.com", result);
  }

  @Test
  void testIsValidHostLabel() {
    assertTrue(ZoneStringUtil.isValidHostLabel("my-host"));
    assertTrue(ZoneStringUtil.isValidHostLabel("müller"));
    assertTrue(ZoneStringUtil.isValidHostLabel("xn--mller-kva"));
  }

  @Test
  void testRejectsMultiLabelHostNames() {
    assertFalse(ZoneStringUtil.isValidHostLabel("host.subdomain"));
    assertFalse(ZoneStringUtil.isValidHostLabel("host。subdomain"));
    assertFalse(ZoneStringUtil.isValidHostLabel("host．subdomain"));
    assertFalse(ZoneStringUtil.isValidHostLabel("host｡subdomain"));
    assertFalse(ZoneStringUtil.isValidHostLabel(""));
    assertFalse(ZoneStringUtil.isValidHostLabel("-invalid"));
    assertFalse(ZoneStringUtil.isValidHostLabel(null));
  }
}
