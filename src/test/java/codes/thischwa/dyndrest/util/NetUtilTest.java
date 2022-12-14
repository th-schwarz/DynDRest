package codes.thischwa.dyndrest.util;

import codes.thischwa.dyndrest.model.IpSetting;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class NetUtilTest {

	@Test
	final void testResolve() throws IOException {
		IpSetting ips = NetUtil.resolve("mein-email-fach.de");
		assertEquals("IpSetting(ipv4=mein-email-fach.de./188.68.45.198, ipv6=mein-email-fach.de./2a03:4000:41:32:0:0:0:2)", ips.toString());
	}

	@Test
	final void testIsIp() {
		assertTrue(NetUtil.isIP("188.68.45.198"));
		assertFalse(NetUtil.isIP("188.68.45.265"));

		assertTrue(NetUtil.isIP("2a03:4000:41:32:0:0:0:2"));
		assertFalse(NetUtil.isIP("2a03:4000:41:32:0:0:0:2h"));
	}

	@Test
	final void testIsIpv4() {
		assertTrue(NetUtil.isIPv4("188.68.45.198"));
		assertFalse(NetUtil.isIPv4("188.68.45.265"));
	}

	@Test
	final void testIsIpv6() {
		assertTrue(NetUtil.isIPv6("2a03:4000:41:32:0:0:0:2"));
		assertFalse(NetUtil.isIPv6("2a03:4000:41:32:0:0:0:2h"));
	}
}
