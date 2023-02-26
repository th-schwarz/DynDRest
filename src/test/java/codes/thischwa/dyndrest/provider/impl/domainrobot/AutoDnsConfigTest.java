package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoDnsConfigTest extends GenericIntegrationTest {

	@Autowired
	private AutoDnsConfig config;

	@Test
	final void testConfig() {
		assertEquals("https://api.autodns.com/v1", config.url());
		assertEquals("user_t", config.user());
		assertEquals(4, config.context());
		assertEquals("pwd_t", config.password());
	}

}
