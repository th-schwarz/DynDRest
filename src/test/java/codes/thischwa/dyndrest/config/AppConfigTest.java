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
    assertEquals("dump.sql", db.dumpFile());

    AppConfig.Database.Backup bck = db.backup();
    assertNotNull(bck);
    assertTrue(bck.enabled());
    assertEquals("./backup", bck.path());
    assertEquals("0 30 4 * * SUN", bck.cron());

    AppConfig.Database.Restore rst = db.restore();
    assertNotNull(rst);
    assertTrue(rst.enabled());
    assertEquals("./restore", rst.path());
  }
}
