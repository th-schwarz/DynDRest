package codes.thischwa.dyndrest.config;

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
  }

  @Test
  final void testDatabase() {
    AppConfig.Database db = appConfig.database();
    assertNotNull(db);
    assertEquals("org.h2.Driver", db.driverClassName());
    assertEquals("jdbc:h2:file:", db.jdbcUrlPrefix());
    assertEquals("./test-db", db.file());
    assertEquals("dba", db.user());
    assertEquals("", db.password());
  }
}
