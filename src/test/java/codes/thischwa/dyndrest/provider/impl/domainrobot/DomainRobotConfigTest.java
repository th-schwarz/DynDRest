package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class DomainRobotConfigTest extends GenericIntegrationTest {

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
	void testDefaultTtl() {
		assertEquals(61L, config.defaultTtl());
	}
}