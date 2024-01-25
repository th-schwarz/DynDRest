package codes.thischwa.dyndrest.config;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.service.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppConfigTest extends GenericIntegrationTest {

  @Autowired private AppConfig appConfig;

  @Test
  final void testConfig() {
    assertFalse(appConfig.hostValidationEnabled());
    assertTrue(appConfig.greetingEnabled());

    assertEquals(201, appConfig.updateIpChangedStatus());

    assertEquals("domainrobot", appConfig.provider());

    assertEquals(
        "file:target/test-classes/test-files/dyndrest-update*", appConfig.updateLogFilePattern());
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
