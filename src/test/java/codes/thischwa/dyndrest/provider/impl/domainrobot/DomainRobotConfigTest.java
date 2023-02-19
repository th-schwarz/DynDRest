package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class DomainRobotConfigTest extends GenericIntegrationTest {

	private final int configuredEntries = 2;

	@Autowired private DomainRobotConfig config;

	@Test
	final void testCountZones() {
		assertEquals(configuredEntries, config.getZones().size());
	}

	@Test
	final void testZoneDetails() {
		DomainRobotConfig.Zone zone = config.getZones().get(0);
		assertEquals("dynhost0.info", zone.getName());
		assertEquals("ns0.domain.info", zone.getNs());

		assertEquals("my0:1234567890abcdef", zone.getHosts().get(0));
		assertEquals("test0:1234567890abcdx", zone.getHosts().get(1));
	}

}