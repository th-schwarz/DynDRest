package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainRobotConfiguratorTest extends GenericIntegrationTest {

	private final int configuredEntries = 2;

	@Autowired
	private DomainRobotConfigurator config;

	@BeforeEach
	void setUp() {
		config.read();
	}

	@Test
	void testDefaulTtl() {
		assertEquals(61L, config.getDefaultTtl());
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
		DomainRobotConfig.Zone z = config.getDomainRobotConfig().zones().get(0);
		z.hosts().add(wrongHost);
		assertThrows(IllegalArgumentException.class, config::read);
		z.hosts().remove(wrongHost);
	}

	@Test
	final void testEmptyHosts() {
		DomainRobotConfig.Zone z = config.getDomainRobotConfig().zones().get(1);
		List<String> hosts = new ArrayList<>(z.hosts());
		z.hosts().clear();
		assertThrows(IllegalArgumentException.class, config::read);
		z.hosts().addAll(hosts);
	}
}
