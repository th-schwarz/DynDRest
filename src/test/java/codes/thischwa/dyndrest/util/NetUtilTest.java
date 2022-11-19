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
}
