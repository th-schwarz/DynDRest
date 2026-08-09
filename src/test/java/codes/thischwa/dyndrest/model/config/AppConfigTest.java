package codes.thischwa.dyndrest.model.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AppConfigTest.EmptyConfig.class)
@EnableConfigurationProperties(AppConfig.class)
@ActiveProfiles("test")
class AppConfigTest {

  @Configuration
  static class EmptyConfig {}

  @Autowired
  private AppConfig appConfig;

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

    assertEquals(30, appConfig.zoneUpdateSchedulerIntervalSeconds());
  }
}
