package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.config.PostProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** This class provides database restore functionality based on the provided configuration. */
@Service
@Profile("!test")
@Slf4j
public class DatabaseRestoreHandler extends PostProcessor {

  private JdbcTemplate jdbcTemplate;

  private boolean dbExists;

  private boolean restoreEnabled;

  @Nullable private Path restorePath = null;

  @Nullable private Path restorePathBak = null;

  @Override
  public void process(Collection<Object> wantedBeans) throws Exception {
    AppConfig appConfig = (AppConfig) wantedBeans.stream()
          .filter(bean -> bean instanceof AppConfig)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("AppConfig not found."));
    DataSource dataSource = (DataSource) wantedBeans.stream()
            .filter(bean -> bean instanceof DataSource)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Datasource not found."));
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
    restore();
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
        renameDump();
      }
    } else {
      log.info("Embedded database is empty, try restore it from the last dump!");
      populate(ds);
      renameDump();
    }
  }

  private void renameDump() {
    try {
      Files.move(restorePath, restorePathBak, StandardCopyOption.REPLACE_EXISTING);
      log.info("Database restored successful, restore dump has moved to: {}", restorePathBak);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isDatabaseEmpty() {
    String query = "SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
    Integer count = jdbcTemplate.queryForObject(query, Integer.class);
    return count == null || count == 0;
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

  @Override
  public Class<?>[] getWanted() {
    return new Class<?>[] {AppConfig.class, DataSource.class};
  }
}
