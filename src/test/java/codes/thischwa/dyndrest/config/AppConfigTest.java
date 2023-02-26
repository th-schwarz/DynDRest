package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest extends GenericIntegrationTest {

	@Autowired
	private AppConfig config;

	@Test
	final void test() {
		assertFalse(config.hostValidationEnabled());
		assertTrue(config.greetingEnabled());

		assertEquals("domainrobot", config.provider());

		assertEquals("file:target/test-classes/test-files/dyndrest-update*", config.updateLogFilePattern());
		assertEquals("(.*)\\s+-\\s+([a-zA-Z\\.-]*)\\s+(\\S*)\\s+(\\S*)", config.updateLogPattern());
		assertEquals("yyyy-MM-dd HH:mm:SSS", config.updateLogDatePattern());
		assertTrue(config.updateLogPageEnabled());
		assertEquals(4, config.updateLogPageSize());
		assertEquals("log-dev", config.updateLogUserName());
		assertEquals("l0g-dev", config.updateLogUserPassword());
	}
}
