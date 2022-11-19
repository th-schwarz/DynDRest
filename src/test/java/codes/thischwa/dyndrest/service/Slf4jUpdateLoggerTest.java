package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.IpSetting;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Slf4jUpdateLoggerTest {

	@Test
	final void testBuildLogEntry() throws UnknownHostException {
		assertEquals("mydyndns.domain.org         127.1.2.4  2a03:4000:41:32:0:0:0:3",
				Slf4jUpdateLogger.buildLogEntry("%s  %16s  %s", "mydyndns.domain.org", new IpSetting("127.1.2.4", "2a03:4000:41:32:0:0:0:3")));
		assertEquals("mydyndns.domain.org         127.1.2.4  n/a",
				Slf4jUpdateLogger.buildLogEntry("%s  %16s  %s", "mydyndns.domain.org", new IpSetting("127.1.2.4", null)));
		assertEquals("mydyndns.domain.org               n/a  2a03:4000:41:32:0:0:0:3",
				Slf4jUpdateLogger.buildLogEntry("%s  %16s  %s", "mydyndns.domain.org", new IpSetting(null, "2a03:4000:41:32:0:0:0:3")));
	}

}
