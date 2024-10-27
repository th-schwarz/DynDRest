package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.config.database.DatabaseRestoreConfig;
import codes.thischwa.dyndrest.model.config.database.DatabaseServiceConfig;
import codes.thischwa.dyndrest.server.config.BeanCollector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** This class provides database restore functionality based on the provided configuration. */
@Component
@Slf4j
public class DatabaseRestoreHandler extends BeanCollector {

  private static final Class<?>[] wantedBeans =
      new Class<?>[] {DatabaseServiceConfig.class, DatabaseRestoreConfig.class, DataSource.class};

  private final Environment env;

  @Nullable private JdbcTemplate jdbcTemplate;

    private boolean dbExists;

  private boolean restoreEnabled;

  @Nullable private Path restorePath = null;

  @Nullable private Path restorePathBak = null;

  public DatabaseRestoreHandler(Environment env) {
    this.env = env;
  }

  @Override
  public void process(Collection<Object> wantedBeans) throws BeansException {
    log.info("entered #process");
    // don't know why @Profil don't work
    if (env.matchesProfiles("test", "opendoc")) {
      log.info("Skip processing, not in production profile!");
      return;
    }
    setupRestorationParams(wantedBeans);
    if (restoreEnabled) {
      try {
        restore();
      } catch (Exception e) {
        log.error("Error while restoring database", e);
        throw new FatalBeanException("Exception while trying to restore the database.", e);
      }
    } else if (!dbExists) {
      log.info(
          "No embedded database found and restore is disabled, the default schema will be restored by liquibase.");
    }
  }

  private void setupRestorationParams(Collection<Object> wantedBeans) {
    jdbcTemplate = new JdbcTemplate(getDataSource(wantedBeans));
    dbExists = !isDatabaseEmpty();
    DatabaseServiceConfig databaseConfig = getDatabaseServiceConfig(wantedBeans);
    DatabaseRestoreConfig databaseRestoreConfig = getDatabaseRestoreConfig(wantedBeans);
    restoreEnabled = databaseRestoreConfig.enabled();
    restorePath = Paths.get(databaseRestoreConfig.path(), databaseConfig.dumpFile()).normalize();

    if (Files.exists(restorePath)) {
      restorePathBak =
          Paths.get(databaseRestoreConfig.path(), databaseConfig.dumpFile() + ".bak").normalize();
      log.info("Database restore enabled and path exists: {}", restorePath);
    } else {
      log.warn("Database restore is enabled, but path does not exist: {}", restorePath);
      restoreEnabled = false;
      restorePath = null;
    }
  }

  private DatabaseServiceConfig getDatabaseServiceConfig(Collection<Object> wantedBeans) {
    return (DatabaseServiceConfig)
        wantedBeans.stream()
            .filter(DatabaseServiceConfig.class::isInstance)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("DatabaseServiceConfig not found."));
  }

  private DatabaseRestoreConfig getDatabaseRestoreConfig(Collection<Object> wantedBeans) {
    return (DatabaseRestoreConfig)
        wantedBeans.stream()
            .filter(DatabaseRestoreConfig.class::isInstance)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("DatabaseRestoreConfig not found."));
  }

  private DataSource getDataSource(Collection<Object> wantedBeans) {
    return (DataSource)
        wantedBeans.stream()
            .filter(DataSource.class::isInstance)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Datasource not found."));
  }

  /**
   * Restores the embedded database. The dump file will be renamed.
   *
   * @throws Exception if an error occurs during the restore process.
   */
  public void restore() throws Exception {
    DataSource ds = jdbcTemplate.getDataSource();
    assert ds != null;
    log.info("Database restore is enabled, try to restore it from the last dump.");
    populate(ds);
    renameDump();
  }

  private void renameDump() throws IOException {
    assert restorePath != null;
    assert restorePathBak != null;
    Files.move(restorePath, restorePathBak, StandardCopyOption.REPLACE_EXISTING);
  }

  private boolean isDatabaseEmpty() {
    String query = "SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
    Integer count = jdbcTemplate.queryForObject(query, Integer.class);
    return count == null || count == 0;
  }

  private void populate(DataSource ds) {
    Resource dbResource = new FileSystemResource(restorePath);
    ResourceDatabasePopulator resourceDatabasePopulator =
        new ResourceDatabasePopulator(false, false, "UTF-8", dbResource);
    resourceDatabasePopulator.execute(ds);
    log.info("Database successful populated from {}", dbResource);
  }

  @Override
  public Class<?>[] getWanted() {
    return wantedBeans;
  }
}
