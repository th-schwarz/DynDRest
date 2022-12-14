package codes.thischwa.dyndrest.provider;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.IpSetting;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProviderTest extends GenericIntegrationTest {

	@Test
	final void test() throws ProviderException {
		TestProvider provider = new TestProvider();
		IpSetting ips = provider.info("mein-email-fach.de");

		assertEquals("IpSetting(ipv4=mein-email-fach.de./188.68.45.198, ipv6=mein-email-fach.de./2a03:4000:41:32:0:0:0:2)", ips.toString());
	}

	static class TestProvider extends GenericProvider {

		@Override
		public void validateHostConfiguration() throws IllegalArgumentException {
		}

		@Override
		public Set<String> getConfiguredHosts() {
			return null;
		}

		@Override
		public void update(String host, IpSetting ipSetting) throws ProviderException {
		}

		@Override
		public String getApitoken(String host) {
			return null;
		}
	}
}
