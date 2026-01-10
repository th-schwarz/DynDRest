package codes.thischwa.dyndrest.model;

import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class IpSettingTest {

    @Test
    void defaultConstructor_isNotSet() {
        IpSetting s = new IpSetting();
        assertTrue(s.isNotSet());
        assertNull(s.getIpv4());
        assertNull(s.getIpv6());
        assertNull(s.ipv4ToString());
        assertNull(s.ipv6ToString());
    }

    @Test
    void stringConstructor_setsIpv4() throws UnknownHostException {
        IpSetting s = new IpSetting("192.168.1.10");
        assertFalse(s.isNotSet());
        assertNotNull(s.getIpv4());
        assertNull(s.getIpv6());
        assertEquals("192.168.1.10", s.ipv4ToString());
    }

    @Test
    void stringConstructor_setsIpv6() throws UnknownHostException {
        IpSetting s = new IpSetting("2a03:4000:41:32::2");
        assertFalse(s.isNotSet());
        assertNull(s.getIpv4());
        assertNotNull(s.getIpv6());
        assertEquals("2a03:4000:41:32:0:0:0:2", s.ipv6ToString());
    }

    @Test
    void dualStringConstructor_setsBoth_whenValid() throws UnknownHostException {
        IpSetting s = new IpSetting("10.0.0.1", "2a03:4000:41:32::20");
        assertFalse(s.isNotSet());
        assertEquals("10.0.0.1", s.ipv4ToString());
        assertEquals("2a03:4000:41:32:0:0:0:20", s.ipv6ToString());
    }

    @Test
    void dualStringConstructor_handlesNullsIndividually() throws UnknownHostException {
        IpSetting onlyV4 = new IpSetting("10.0.0.2", null);
        assertEquals("10.0.0.2", onlyV4.ipv4ToString());
        assertNull(onlyV4.ipv6ToString());

        IpSetting onlyV6 = new IpSetting(null, "2a03:4000:41:32::21");
        assertNull(onlyV6.ipv4ToString());
        assertEquals("2a03:4000:41:32:0:0:0:21", onlyV6.ipv6ToString());
    }

    @Test
    void inetAddressConstructor_setsOnlyMatchingTypes() throws Exception {
        InetAddress v4 = InetAddress.getByName("172.16.0.3");
        InetAddress v6 = InetAddress.getByName("2a03:4000:41:32::22");
        IpSetting s = new IpSetting(v4, v6);
        assertEquals("172.16.0.3", s.ipv4ToString());
        assertEquals("2a03:4000:41:32:0:0:0:22", s.ipv6ToString());

        // Pass swapped types to ensure non-matching are ignored
        Inet4Address onlyV4 = (Inet4Address) v4;
        Inet6Address onlyV6 = (Inet6Address) v6;
        IpSetting s2 = new IpSetting(onlyV6, onlyV4); // wrong order on purpose
        // constructor should ignore mismatched types, leaving nulls
        assertNull(s2.getIpv4());
        assertNull(s2.getIpv6());
    }

    @Test
    void equalsAndHashCode_sameIps_areEqual() throws UnknownHostException {
        IpSetting a = new IpSetting("10.0.0.1", "2a03:4000:41:32::23");
        IpSetting b = new IpSetting("10.0.0.1", "2a03:4000:41:32::23");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsAndHashCode_differentIps_notEqual() throws UnknownHostException {
        IpSetting a = new IpSetting("10.0.0.1", "2a03:4000:41:32::23");
        IpSetting b = new IpSetting("10.0.0.2", "2a03:4000:41:32::23");
        assertNotEquals(a, b);
    }

    @Test
    void toString_containsAddresses_whenSet() throws UnknownHostException {
        IpSetting s = new IpSetting("10.0.0.5", "2a03:4000:41:32::24");
        String txt = s.toString();
        assertTrue(txt.contains("10.0.0.5"));
        assertTrue(txt.contains("2a03:4000:41:32"));
    }
}
