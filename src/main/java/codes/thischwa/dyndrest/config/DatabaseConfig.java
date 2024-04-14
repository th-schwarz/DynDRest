package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.model.converter.EnumToStringConverter;
import codes.thischwa.dyndrest.model.converter.StringToEnumConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The Database configuration. <br>
 * The {@link DataSource} will be generated and if the embedded database wasn't exists, the database
 * will be populated with the basic schema.
 */
@Configuration
@EnableTransactionManagement
@Profile("!test")
@Slf4j
public class DatabaseConfig extends AbstractJdbcConfiguration {

  @Nullable private final AppConfig.Database databaseConfig;

  private final boolean dbExists;

  private boolean restoreEnabled;

  @Nullable private Path restorePath = null;

  @Nullable private Path restorePathBak = null;

  /** The DatabaseConfig class provides the configuration for the application's database. */
  public DatabaseConfig(AppConfig appConfig) {
    this.databaseConfig = appConfig.database();
    String search = databaseConfig.file() + ".mv.db";
    Path dbPath = Paths.get(search);
    dbExists = Files.exists(dbPath);

    AppConfig.Database.Restore dbRestore = databaseConfig.restore();
    if (dbRestore == null) {
      return;
    }
    if (dbRestore.enabled()) {
      restorePath = Paths.get(dbRestore.path(), databaseConfig.dumpFile());
      if (Files.exists(restorePath)) {
        log.info("Database restore enabled and path exists: {}", restorePath);
        restoreEnabled = true;
        restorePathBak = Paths.get(dbRestore.path(), databaseConfig.dumpFile() + ".bak");
      } else {
        log.info("Database restore enabled, but path does not exist: {}", restorePath);
      }
    } else {
      restoreEnabled = false;
    }
  }

  /**
   * Retrieves the DataSource for the application, If the embedded database wasn't exists, the
   * database will be populated with the basic schema.
   *
   * @return The DataSource object.
   */
  @Bean
  public DataSource getDataSource() {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName(databaseConfig.driverClassName());
    dataSourceBuilder.url(databaseConfig.jdbcUrlPrefix() + databaseConfig.file());
    dataSourceBuilder.username(databaseConfig.user());
    dataSourceBuilder.password(databaseConfig.password());
    DataSource ds = dataSourceBuilder.build();
    if (dbExists) {
      log.info("Embedded database found: {}", databaseConfig.file());
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
      populate(ds);
    }
    return ds;
  }

  private void populate(DataSource ds) {
    Resource dBResource =
            restoreEnabled
                    ? new FileSystemResource(restorePath)
                    : new ClassPathResource("h2/schema.sql");
    ResourceDatabasePopulator resourceDatabasePopulator =
            new ResourceDatabasePopulator(false, false, "UTF-8", dBResource);
    resourceDatabasePopulator.execute(ds);
    log.info("Database successful populated to {}", dBResource);
  }

  @Bean
  NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }

  @Bean
  TransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Override
  protected List<?> userConverters() {
    return Arrays.asList(
        new EnumToStringConverter<Enum<?>>(), new StringToEnumConverter<>(UpdateLog.Status.class));
  }
}
