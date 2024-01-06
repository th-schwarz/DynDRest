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
	private AppConfig appConfig;

	@Autowired
	private ZoneConfig zoneConfig;

	@Autowired
	private AppConfigurator configurator;

	@Test
	final void testConfig() {
		assertFalse(appConfig.hostValidationEnabled());
		assertTrue(appConfig.greetingEnabled());

		assertEquals(201, appConfig.updateIpChangedStatus());

		assertEquals("domainrobot", appConfig.provider());

		assertEquals("file:target/test-classes/test-files/dyndrest-update*", appConfig.updateLogFilePattern());
		assertEquals("(.*)\\s+-\\s+([a-zA-Z\\.-]*)\\s+(\\S*)\\s+(\\S*)", appConfig.updateLogPattern());
		assertEquals("%d{yyyy-MM-dd HH:mm:ss.SSS} - %msg%n", appConfig.updateLogEncoderPattern());
		assertEquals("yyyy-MM-dd HH:mm:SSS", appConfig.updateLogDatePattern());
		assertTrue(appConfig.updateLogPageEnabled());
		assertEquals(4, appConfig.updateLogPageSize());
		assertEquals("log-dev", appConfig.updateLogUserName());
		assertEquals("l0g-dev", appConfig.updateLogUserPassword());

		assertEquals("health", appConfig.healthCheckUserName());
		assertEquals("hea1th", appConfig.healthCheckUserPassword());
	}

	@Test
	final void testCountZones() {
		assertEquals(configuredEntries, zoneConfig.zones().size());
	}

	@Test
	final void testZoneDetails() {
		ZoneConfig.Zone zone = zoneConfig.zones().get(0);
		assertEquals("dynhost0.info", zone.name());
		assertEquals("ns0.domain.info", zone.ns());

		assertEquals("my0:1234567890abcdef", zone.hosts().get(0));
		assertEquals("test0:1234567890abcdx", zone.hosts().get(1));
	}


	@Test
	final void testWrongHostFormat() {
		String wrongHost = "wrong-formatted.host";
		ZoneConfig.Zone z = zoneConfig.zones().get(0);
		z.hosts().add(wrongHost);
		assertThrows(IllegalArgumentException.class, configurator::read);
		z.hosts().remove(wrongHost);
	}

	@Test
	final void testEmptyHosts() {
		ZoneConfig.Zone z = zoneConfig.zones().get(1);
		List<String> hosts = new ArrayList<>(z.hosts());
		z.hosts().clear();
		assertThrows(IllegalArgumentException.class, configurator::read);
		z.hosts().addAll(hosts);
	}
}
