package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codes.thischwa.dyndrest.GenericIntegrationTest;

class AutoDnsConfigTest extends GenericIntegrationTest {

	@Autowired
	private AutoDnsConfig config;

	@Test
	final void testGetUrl() {
		assertEquals("https://api.autodns.com/v1", config.getUrl());
	}

	@Test
	final void testGetContext() {
		assertEquals(4, config.getContext());
	}

	@Test
	final void testGetUser() {
		assertEquals("user_t", config.getUser());
	}

	@Test
	final void testGetPassword() {
		assertEquals("pwd_t", config.getPassword());
	}

}
