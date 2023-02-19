package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest extends GenericIntegrationTest {

	@Autowired private AppConfig appConfig;

	@Test
	final void test() {
		assertEquals("domainrobot", appConfig.getProvider());
		assertFalse(appConfig.isHostValidationEnabled());
		assertTrue(appConfig.isUpdateLogPageEnabled());
		assertEquals("log-dev", appConfig.getUpdateLogUserName());
		assertEquals("l0g-dev", appConfig.getUpdateLogUserPassword());
	}

}
