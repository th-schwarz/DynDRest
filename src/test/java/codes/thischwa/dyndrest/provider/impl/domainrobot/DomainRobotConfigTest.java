package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DomainRobotConfigTest extends AbstractIntegrationTest {

	@Autowired private DomainRobotConfig config;

	@Test
	final void testAutoDnsConfig() {
		DomainRobotConfig.Autodns autodns = config.autodns();
		assertEquals("https://api.autodns.com/v1", autodns.url());
		assertEquals("user_t", autodns.user());
		assertEquals(4, autodns.context());
		assertEquals("pwd_t", autodns.password());
	}

	@Test
	final void testDefaultTtl() {
		assertEquals(61L, config.defaultTtl());
	}

	@Test
	final void testHeader() {
		Map<String, String> header = config.customHeader();
		assertTrue(header.containsKey("X-Domainrobot-WS"));
		assertEquals("NONE", header.get("X-Domainrobot-WS"));
    	assertTrue(header.containsKey("X-Domainrobot-Stid"));
		assertEquals("my-stid-123", header.get("X-Domainrobot-Stid"));
	}
}