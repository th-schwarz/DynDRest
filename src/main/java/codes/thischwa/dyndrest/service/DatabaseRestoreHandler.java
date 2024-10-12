package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.config.AppConfig;
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
@Profile({"!opendoc"})
@Slf4j
public class DatabaseRestoreHandler extends BeanCollector {

  private JdbcTemplate jdbcTemplate;

  private boolean dbExists;

  private boolean restoreEnabled;

  @Nullable private Path restorePath = null;

  @Nullable private Path restorePathBak = null;

  @Override
  public void process(Collection<Object> wantedBeans) throws BeansException {
    log.info("entered #process");
    setupRestorationParams(wantedBeans);
    if ((!dbExists && !restoreEnabled) || (dbExists && restoreEnabled)) {
        try {
            restore();
        } catch (Exception e) {
          log.error("Error while restoring database", e);
        throw new FatalBeanException("Exception while trying to restore the database.", e);
        }
    }
  }

  private void setupRestorationParams(Collection<Object> wantedBeans) {
    AppConfig appConfig = getAppConfig(wantedBeans);
    jdbcTemplate = new JdbcTemplate(getDataSource(wantedBeans));
    dbExists = !isDatabaseEmpty();

    AppConfig.Database databaseConfig = appConfig.database();
    AppConfig.Database.Restore dbRestore = databaseConfig.restore();

    if (dbRestore != null && dbRestore.enabled()) {
      setupRestorePath(databaseConfig, dbRestore);
    }
  }

  private AppConfig getAppConfig(Collection<Object> wantedBeans) {
    return (AppConfig)
        wantedBeans.stream()
            .filter(AppConfig.class::isInstance)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("AppConfig not found."));
  }

  private DataSource getDataSource(Collection<Object> wantedBeans) {
    return (DataSource)
        wantedBeans.stream()
            .filter(DataSource.class::isInstance)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Datasource not found."));
  }

  private void setupRestorePath(
      AppConfig.Database databaseConfig, AppConfig.Database.Restore dbRestore) {
    restorePath = Paths.get(dbRestore.path(), databaseConfig.dumpFile()).normalize();

    if (Files.exists(restorePath)) {
      restoreEnabled = true;
      restorePathBak = Paths.get(dbRestore.path(), databaseConfig.dumpFile() + ".bak").normalize();
      log.info("Database restore enabled and path exists: {}", restorePath);
    } else {
      log.warn("Database restore is enabled, but path does not exist: {}", restorePath);
      restoreEnabled = false;
      restorePath = null;
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
        log.info("Database exists and restore is enabled, try to restore it from the last dump.");
        populate(ds);
        renameDump();
      }
    } else {
      if (restoreEnabled) {
        log.info(
            "Embedded database doesn't exists and restore is enabled, " +
                    "try restore it from the last dump!");
        populate(ds);
        renameDump();
      } else {
        log.info(
            "Embedded database doesn't exists and restore is disabled, try restore the schema!");
        populate(ds);
      }
    }
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
