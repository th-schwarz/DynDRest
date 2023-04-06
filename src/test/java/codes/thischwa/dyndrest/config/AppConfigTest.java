package codes.thischwa.dyndrest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
		assertEquals("%d{yyyy-MM-dd HH:mm:ss.SSS} - %msg%n", config.updateLogEncoderPattern());
		assertEquals("yyyy-MM-dd HH:mm:SSS", config.updateLogDatePattern());
		assertTrue(config.updateLogPageEnabled());
		assertEquals(4, config.updateLogPageSize());
		assertEquals("log-dev", config.updateLogUserName());
		assertEquals("l0g-dev", config.updateLogUserPassword());
	}
}
