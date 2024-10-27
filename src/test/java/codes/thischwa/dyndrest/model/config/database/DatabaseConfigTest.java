package codes.thischwa.dyndrest.model.config.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DatabaseConfigTest extends AbstractIntegrationTest {

  @Autowired private DatabaseServiceConfig databaseServiceConfig;
  @Autowired private DatabaseBackupConfig databaseBackupConfig;
  @Autowired private DatabaseRestoreConfig databaseRestoreConfig;

  @Test
  void testService() {
    assertEquals("dump.sql", databaseServiceConfig.dumpFile());
  }

  @Test
  void testBackup() {
    assertEquals("./backup", databaseBackupConfig.path());
    assertEquals("0 30 4 * * SUN", databaseBackupConfig.cron());
    assertFalse(databaseBackupConfig.enabled());
  }

  @Test
  void testRestore() {
    assertEquals("./restore", databaseRestoreConfig.path());
    assertTrue(databaseRestoreConfig.enabled());
  }
}
