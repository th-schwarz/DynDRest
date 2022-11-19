package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codes.thischwa.dyndrest.GenericIntegrationTest;

class ZoneHostConfigTest extends GenericIntegrationTest {

	private final int configuredEntries = 2;

	@Autowired private ZoneHostConfig config;

	@BeforeEach
	void setUp() {
		config.read();
	}

	@Test
	final void testCountZones() {
		assertEquals(configuredEntries, config.getZones().size());
	}

	@Test
	final void testZoneDetails() {
		ZoneHostConfig.Zone zone = config.getZones().get(0);
		assertEquals("dynhost0.info", zone.getName());
		assertEquals("ns0.domain.info", zone.getNs());

		assertEquals("my0:1234567890abcdef", zone.getHosts().get(0));
		assertEquals("test0:1234567890abcdx", zone.getHosts().get(1));
	}

	@Test
	final void testGetApiToken() {
		assertEquals("1234567890abcdef", config.getApitoken("my0.dynhost0.info"));
		assertThrows(IllegalArgumentException.class, () -> config.getApitoken("unknown.host.info"));
	}

	@Test
	final void testgetPrimaryNameServer() {
		assertEquals("ns1.domain.info", config.getPrimaryNameServer("dynhost1.info"));
		assertThrows(IllegalArgumentException.class, () -> config.getPrimaryNameServer("unknown-host.info"));
	}

	@Test
	final void testConfigured() {
		assertEquals(configuredEntries * 2, config.getConfiguredHosts().size());
		assertEquals(configuredEntries, config.getConfiguredZones().size());
	}

	@Test
	final void testValidateData_ok() {
		config.validate();
	}

	@Test
	final void testWrongHostFormat() {
		String wrongHost = "wrong-formatted.host";
		ZoneHostConfig.Zone z = config.getZones().get(0);
		z.getHosts().add(wrongHost);
		assertThrows(IllegalArgumentException.class, config::read);
		z.getHosts().remove(wrongHost);
	}

	@Test
	final void testEmptyHosts() {
		ZoneHostConfig.Zone z = config.getZones().get(1);
		List<String> hosts = new ArrayList<>(z.getHosts());
		z.getHosts().clear();
		assertThrows(IllegalArgumentException.class, config::read);
		z.setHosts(hosts);
	}
}
