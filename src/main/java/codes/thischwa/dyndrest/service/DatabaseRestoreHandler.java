package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.config.AppConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.lang.Nullable;

/** This class provides database restore functionality based on the provided configuration. */
@Slf4j
public class DatabaseRestoreHandler {

  private final JdbcTemplate jdbcTemplate;

  private final boolean dbExists;

  private boolean restoreEnabled;

  @Nullable private Path restorePath = null;

  @Nullable private Path restorePathBak = null;

  /**
   * This method restores the database based on the provided configuration. If the database is
   * empty, it populates it with the provided schema file (either from the restore path or a default
   * classpath resource). If the restore functionality is enabled and the restore path exists, it
   * restores the database from the dump file and moves the dump file to a backup location.
   */
  public DatabaseRestoreHandler(AppConfig appConfig, DataSource dataSource) {
    AppConfig.Database databaseConfig = appConfig.database();
    this.jdbcTemplate = new JdbcTemplate(dataSource);

    dbExists = !isDatabaseEmpty();
    AppConfig.Database.Restore dbRestore = databaseConfig.restore();
    if (dbRestore == null) {
      restoreEnabled = false;
      return;
    }
    if (dbRestore.enabled()) {
      restorePath = Paths.get(dbRestore.path(), databaseConfig.dumpFile()).normalize();
      if (Files.exists(restorePath)) {
        log.info("Database restore enabled and path exists: {}", restorePath);
        restoreEnabled = true;
        restorePathBak =
            Paths.get(dbRestore.path(), databaseConfig.dumpFile() + ".bak").normalize();
      } else {
        log.warn("Database restore is enabled, but path does not exist: {}", restorePath);
      }
    } else {
      restoreEnabled = false;
    }
  }

  /**
   * Restores the embedded database. The dump file will be renamed.
   *
   * @throws Exception if an error occurs during the restore process.
   */
  public void restore() throws Exception {
    DataSource ds = jdbcTemplate.getDataSource();
    assert ds != null;
    if (dbExists) {
      log.info("Embedded database found.");
      if (restoreEnabled) {
        assert restorePath != null;
        assert restorePathBak != null;
        populate(ds);
        try {
          Files.move(restorePath, restorePathBak, StandardCopyOption.REPLACE_EXISTING);
          log.info("Database restored successful, restore dump has moved to: {}", restorePathBak);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      log.info("Embedded database not found!");
      populate(ds);
    }
  }

  private boolean isDatabaseEmpty() {
    String query = "SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
    Integer count = jdbcTemplate.queryForObject(query, Integer.class);
    return count == 0;
  }

  private void populate(DataSource ds) {
    Resource dbResource =
        restoreEnabled
            ? new FileSystemResource(restorePath)
            : new ClassPathResource("h2/schema.sql");
    ResourceDatabasePopulator resourceDatabasePopulator =
        new ResourceDatabasePopulator(false, false, "UTF-8", dbResource);
    resourceDatabasePopulator.execute(ds);
    log.info("Database successful populated from {}", dbResource);
  }
}
