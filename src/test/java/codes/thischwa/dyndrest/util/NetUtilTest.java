package codes.thischwa.dyndrest.util;

import static codes.thischwa.dyndrest.util.NetUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.model.IpSetting;
import java.io.IOException;
import java.net.IDN;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.Name;

class NetUtilTest {

  @Test
  void testResolve() throws IOException {
    IpSetting ips = resolve("mein-email-fach.de");
    assertEquals(
        "IpSetting(ipv4=mein-email-fach.de./37.120.183.96, ipv6=mein-email-fach.de./2a03:4000:4d:e8f:0:0:0:2)",
        ips.toString());
  }

  @Test
  void testIsIp() {
    assertTrue(isIp("188.68.45.198"));
    assertFalse(isIp("188.68.45.265"));

    assertTrue(isIp("2a03:4000:41:32:0:0:0:2"));
    assertFalse(isIp("2a03:4000:41:32:0:0:0:2h"));
  }

  @Test
  void testIsIpv4() {
    assertTrue(isIpv4("188.68.45.198"));
    assertFalse(isIpv4("188.68.45.265"));
  }

  @Test
  void testIsIpv6() {
    assertTrue(isIpv6("2a03:4000:41:32:0:0:0:2"));
    assertFalse(isIpv6("2a03:4000:41:32:0:0:0:2h"));
  }

  @Test
  void testDnsjavaIdn() {
    String idn = "müller.de";
    String ascii = IDN.toASCII(idn);
    assertEquals("m\\252ller.de", Name.fromConstantString(idn).toString());
    assertEquals("xn--mller-kva.de", ascii);
    assertEquals("xn--mller-kva.de", Name.fromConstantString(ascii).toString());

    idn = "平聲";
    ascii = IDN.toASCII(idn);
    assertEquals("xn--gwts07e", ascii);
    assertEquals("xn--gwts07e", Name.fromConstantString(ascii).toString());
    String finalIdn = idn;
    assertThrows(IllegalArgumentException.class, () -> Name.fromConstantString(finalIdn));
  }

  /** Test the buildBasicAuth method with normal inputs */
  @Test
  void testBuildBasicAuth_SimpleValues() {
    String user = "user";
    String password = "password";

    // Call the method and get the result
    String result = NetUtil.buildBasicAuth(user, password);

    // Create the expected output in the same format as the method
    String expected =
        "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());

    // Assert that the expected output and actual output are equal
    assertEquals(expected, result);
  }

  /** Test the buildBasicAuth method with special characters in the username and password */
  @Test
  void testBuildBasicAuth_SpecialCharacters() {
    String user = "user@123!";
    String password = "pass!@#$$%^&*()_+";

    // Call the method and get the result
    String result = NetUtil.buildBasicAuth(user, password);

    // Create the expected output in the same format as the method
    String expected =
        "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());

    // Assert that the expected output and actual output are equal
    assertEquals(expected, result);
  }
}
