package codes.thischwa.dyndrest.model;

import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import static org.junit.jupiter.api.Assertions.*;

class IpSettingTest {

	@Test
	final void testConversion() throws UnknownHostException {
		IpSetting is = new IpSetting("198.0.0.1", "2a03:4000:41:32:0:0:0:1");
		assertEquals(is.ipv4ToString(), is.getIpv4().getHostAddress());
		assertEquals(is.ipv6ToString(), is.getIpv6().getHostAddress());

		is = new IpSetting(null, "2a03:4000:41:32:0:0:0:1");
		assertNull(is.ipv4ToString());
		assertNull(is.getIpv4());
		assertEquals(is.ipv6ToString(), is.getIpv6().getHostAddress());
		assertNull(is.getIpv4());
		assertEquals(is.ipv6ToString(), is.getIpv6().getHostAddress());

		is = new IpSetting("198.0.0.1", null);
		assertEquals(is.ipv4ToString(), is.getIpv4().getHostAddress());

		is = new IpSetting("198.0.0.1", null);
		assertEquals(is.ipv4ToString(), is.getIpv4().getHostAddress());
		assertNull(is.ipv6ToString());
		assertNull(is.getIpv6());
	}

	@Test
	final void constructorTest() throws UnknownHostException {
		IpSetting is = new IpSetting("198.0.0.1");
		assertNull(is.getIpv6());
		assertEquals(is.ipv4ToString(), is.getIpv4().getHostAddress());

		Inet4Address ipv4 = (Inet4Address) InetAddress.getByName("198.0.0.2");
		Inet6Address ipv6 = (Inet6Address) InetAddress.getByName("2a03:4000:41:32::2");
		is = new IpSetting(ipv4, ipv6);
		assertEquals(is.ipv4ToString(), is.getIpv4().getHostAddress());
		assertEquals(is.ipv6ToString(), is.getIpv6().getHostAddress());
	}

	@Test
	final void testException() {
		assertThrows(UnknownHostException.class, () -> new IpSetting("256.0.0.1", "2a03:4000:41:32:0:0:0:1"));
	}
}
