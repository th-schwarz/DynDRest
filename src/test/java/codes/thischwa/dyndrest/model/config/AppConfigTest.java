package codes.thischwa.dyndrest.model.config;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppConfigTest extends AbstractIntegrationTest {

  @Autowired private AppConfig appConfig;

  @Test
  final void testConfig() {
    assertFalse(appConfig.hostValidationEnabled());
    assertTrue(appConfig.greetingEnabled());

    assertEquals(201, appConfig.updateIpChangedStatus());

    assertEquals("domainrobot", appConfig.provider());

    assertEquals("yyyy-MM-dd HH:mm:SSS", appConfig.updateLogDatePattern());
    assertTrue(appConfig.updateLogPageEnabled());
    assertEquals(4, appConfig.updateLogPageSize());
    assertEquals("log-dev", appConfig.updateLogUserName());
    assertEquals("l0g-dev", appConfig.updateLogUserPassword());

    assertEquals("health", appConfig.healthCheckUserName());
    assertEquals("hea1th", appConfig.healthCheckUserPassword());

    assertEquals("admin", appConfig.adminUserName());
    assertEquals("adm1n", appConfig.adminUserPassword());
    assertEquals("token123", appConfig.adminApiToken());
  }
}
