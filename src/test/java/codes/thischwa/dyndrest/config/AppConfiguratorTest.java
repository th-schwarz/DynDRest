package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfiguratorTest extends GenericIntegrationTest {

	private final int configuredEntries = 2;

	@Autowired
	private AppConfigurator configurator;

	@BeforeEach
	void setUp() {
		configurator.read();
	}

	@Test
	final void testGetApiToken() {
		assertEquals("1234567890abcdef", configurator.getApitoken("my0.dynhost0.info"));
		assertThrows(IllegalArgumentException.class, () -> configurator.getApitoken("unknown.host.info"));
	}

	@Test
	final void testGetPrimaryNameServer() {
		assertEquals("ns1.domain.info", configurator.getPrimaryNameServer("dynhost1.info"));
		assertThrows(IllegalArgumentException.class, () -> configurator.getPrimaryNameServer("unknown-host.info"));
	}

	@Test
	final void testConfigured() {
		assertEquals(configuredEntries * 2, configurator.getConfiguredHosts().size());
		assertEquals(configuredEntries, configurator.getConfiguredZones().size());
	}

	@Test
	final void testValidateData_ok() {
		configurator.validate();
	}

}
