package codes.thischwa.dyndrest.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.lang.Nullable;

/**
 * The Database configuration. <br>
 * The {@link DataSource} will be generated and if the embedded database wasn't exists, the database
 * will be populated with the basic schema.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(name = "dyndrest.database.jdbc-url-prefix")
@Slf4j
public class DatabaseConfig {

  @Nullable private final AppConfig.Database databaseConfig;

  private final boolean dbExists;

  /** The DatabaseConfig class provides the configuration for the application's database. */
  public DatabaseConfig(AppConfig appConfig) {
    if (appConfig.database() == null) {
      throw new IllegalArgumentException(
          "Shouldn't be initialized with the current configuration settings.");
    }
    this.databaseConfig = appConfig.database();
    String search = databaseConfig.file() + ".mv.db";
    Path dbPath = Paths.get(search);
    dbExists = Files.exists(dbPath);
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
    } else {
      populate(ds);
      log.info("Embedded database successful built and populated: {}", databaseConfig.file());
    }
    return ds;
  }

  private void populate(DataSource ds) {
    ResourceDatabasePopulator resourceDatabasePopulator =
        new ResourceDatabasePopulator(
            false, false, "UTF-8", new ClassPathResource("h2/schema.sql"));
    resourceDatabasePopulator.execute(ds);
  }
}
