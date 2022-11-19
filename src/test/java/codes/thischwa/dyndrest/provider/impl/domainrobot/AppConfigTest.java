package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest extends GenericIntegrationTest {

	@Autowired
	private AppConfig config;

	@Test
	final void test() {
		assertFalse(config.isHostValidationEnabled());
		assertTrue(config.isGreetingEnabled());

		assertEquals("domainrobot", config.getProvider());

		assertEquals("file:target/test-classes/test-files/dyndrest-update*", config.getUpdateLogFilePattern());
		assertEquals("(.*)\\s+-\\s+([a-zA-Z\\.-]*)\\s+(\\S*)\\s+(\\S*)", config.getUpdateLogPattern());
		assertEquals("yyyy-MM-dd HH:mm:SSS", config.getUpdateLogDatePattern());
		assertTrue(config.isUpdateLogPageEnabled());
		assertEquals(4, config.getUpdateLogPageSize());
		assertEquals("log-dev", config.getUpdateLogUserName());
		assertEquals("l0g-dev", config.getUpdateLogUserPassword());
	}
}
