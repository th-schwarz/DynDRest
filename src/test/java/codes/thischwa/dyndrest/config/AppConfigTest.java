package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfigTest extends GenericIntegrationTest {

	private final int configuredEntries = 2;

	@Autowired
	private AppConfig config;

	@Autowired
	private AppConfigurator configurator;

	@Test
	final void testConfig() {
		assertFalse(config.hostValidationEnabled());
		assertTrue(config.greetingEnabled());

		assertEquals(201, config.updateIpChangedStatus());

		assertEquals("domainrobot", config.provider());

		assertEquals("file:target/test-classes/test-files/dyndrest-update*", config.updateLogFilePattern());
		assertEquals("(.*)\\s+-\\s+([a-zA-Z\\.-]*)\\s+(\\S*)\\s+(\\S*)", config.updateLogPattern());
		assertEquals("%d{yyyy-MM-dd HH:mm:ss.SSS} - %msg%n", config.updateLogEncoderPattern());
		assertEquals("yyyy-MM-dd HH:mm:SSS", config.updateLogDatePattern());
		assertTrue(config.updateLogPageEnabled());
		assertEquals(4, config.updateLogPageSize());
		assertEquals("log-dev", config.updateLogUserName());
		assertEquals("l0g-dev", config.updateLogUserPassword());

		assertEquals("health", config.healthCheckUserName());
		assertEquals("hea1th", config.healthCheckUserPassword());
	}

	@Test
	final void testCountZones() {
		assertEquals(configuredEntries, config.zones().size());
	}

	@Test
	final void testZoneDetails() {
		AppConfig.Zone zone = config.zones().get(0);
		assertEquals("dynhost0.info", zone.name());
		assertEquals("ns0.domain.info", zone.ns());

		assertEquals("my0:1234567890abcdef", zone.hosts().get(0));
		assertEquals("test0:1234567890abcdx", zone.hosts().get(1));
	}


	@Test
	final void testWrongHostFormat() {
		String wrongHost = "wrong-formatted.host";
		AppConfig.Zone z = config.zones().get(0);
		z.hosts().add(wrongHost);
		assertThrows(IllegalArgumentException.class, configurator::read);
		z.hosts().remove(wrongHost);
	}

	@Test
	final void testEmptyHosts() {
		AppConfig.Zone z = config.zones().get(1);
		List<String> hosts = new ArrayList<>(z.hosts());
		z.hosts().clear();
		assertThrows(IllegalArgumentException.class, configurator::read);
		z.hosts().addAll(hosts);
	}
}
