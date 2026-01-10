package codes.thischwa.dyndrest.util;

import static codes.thischwa.dyndrest.util.NetUtil.ipEquals;
import static codes.thischwa.dyndrest.util.NetUtil.isIp;
import static codes.thischwa.dyndrest.util.NetUtil.isIpv4;
import static codes.thischwa.dyndrest.util.NetUtil.isIpv6;
import static codes.thischwa.dyndrest.util.NetUtil.resolve;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.model.IpSetting;
import java.io.IOException;
import java.net.IDN;
import java.net.InetAddress;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.Type;

class NetUtilTest {

  @Test
  void testResolve() throws IOException {
    IpSetting ips = resolve("mein-mail-server.de");
    assertEquals(
        "IpSetting(ipv4=mein-mail-server.de./152.53.130.243, ipv6=mein-mail-server.de./2a0a:4cc0:c0:1e4:0:0:0:1)",
        ips.toString());
  }

  @Test
  void testIpEquals_ValidIpsEqual() {
    String ip1 = "188.68.45.198";
    String ip2 = "188.68.45.198";
    assertTrue(ipEquals(ip1, ip2));
  }

  @Test
  void testIpEquals_ValidIpsNotEqual() {
    String ip1 = "188.68.45.198";
    String ip2 = "188.68.45.199";
    assertFalse(ipEquals(ip1, ip2));
  }

  @Test
  void testIpEquals_InvalidIps() {
    String invalidIp1 = "188.68.45.265";
    String invalidIp2 = "invalid-ip";
    assertThrows(IllegalArgumentException.class, () -> ipEquals(invalidIp1, invalidIp2));
  }

  @Test
  void testIpEquals_ipv6() {
    String ip1 = "2a03:4000:41:32:0:0:0:2";
    String ip2 = "2a03:4000:41:32::2";
    assertTrue(ipEquals(ip1, ip2));
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
    assertEquals("xn--mller-kva.de", ascii);
    assertEquals("xn--mller-kva.de", Name.fromConstantString(ascii).toString());
    assertEquals("m\\252ller.de", Name.fromConstantString(idn).toString());

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

  @Test
  void testLookupFails() {
    assertThrows(IllegalArgumentException.class, () -> NetUtil.lookup("domain.unknowntld" , Type.A));
  }

  @Test
  void testConvert_ValidIp() {
    // Test valid IPv4 address
    String validIpv4 = "192.168.1.1";
    InetAddress resultIpv4 = NetUtil.convert(validIpv4);
    assertEquals(validIpv4, resultIpv4.getHostAddress());

    // Test valid IPv6 address
    String validIpv6 = "2001:db8:85a3:0:0:8a2e:370:7334";
    InetAddress resultIpv6 = NetUtil.convert(validIpv6);
    assertEquals(validIpv6, resultIpv6.getHostAddress());
  }

  @Test
  void testConvert_InvalidIp() {
    // Test invalid IPv4 address
    String invalidIpv4 = "192.168.1.999";
    assertThrows(IllegalArgumentException.class, () -> NetUtil.convert(invalidIpv4));

    // Test invalid IPv6 address
    String invalidIpv6 = "2001:db8:85a3::zzz";
    assertThrows(IllegalArgumentException.class, () -> NetUtil.convert(invalidIpv6));
  }
}
