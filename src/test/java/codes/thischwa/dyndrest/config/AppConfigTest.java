package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest extends GenericIntegrationTest {

	@Autowired private AppConfig config;

	@Test
	final void test() {
		assertEquals("domainrobot", config.getProvider());
		assertFalse(config.isHostValidationEnabled());
		assertTrue(config.isUpdateLogPageEnabled());
		assertEquals("log-dev", config.getUpdateLogUserName());
		assertEquals("l0g-dev", config.getUpdateLogUserPassword());
	}

}
