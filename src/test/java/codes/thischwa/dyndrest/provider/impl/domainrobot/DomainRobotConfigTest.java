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
		assertEquals(configuredEntries, config.zones().size());
	}

	@Test
	final void testZoneDetails() {
		DomainRobotConfig.Zone zone = config.zones().get(0);
		assertEquals("dynhost0.info", zone.name());
		assertEquals("ns0.domain.info", zone.ns());

		assertEquals("my0:1234567890abcdef", zone.hosts().get(0));
		assertEquals("test0:1234567890abcdx", zone.hosts().get(1));
	}

}