package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.provider.impl.domainrobot.DomainRobotConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class DomainRobotConfigTest extends GenericIntegrationTest {

	@Autowired
	private DomainRobotConfig config;

	@Test
	void test() {
		assertEquals(61L, config.getDefaultTtl());
	}
}
