package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.config.converter.EnumToStringConverter;
import codes.thischwa.dyndrest.config.converter.StringToEnumConverter;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
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
@ConditionalOnProperty(name = "dyndrest.database.jdbc-url-prefix")
@Slf4j
public class DatabaseConfig extends AbstractJdbcConfiguration {

  @Nullable private final AppConfig.Database databaseConfig;

  private final boolean dbExists;

  /** The DatabaseConfig class provides the configuration for the application's database. */
  public DatabaseConfig(AppConfig appConfig) {
    this.databaseConfig = appConfig.database();
    String search = Objects.requireNonNull(databaseConfig).file() + ".mv.db";
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

  @Bean
  NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }

  @Bean
  TransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  private void populate(DataSource ds) {
    ResourceDatabasePopulator resourceDatabasePopulator =
        new ResourceDatabasePopulator(
            false, false, "UTF-8", new ClassPathResource("h2/schema.sql"));
    resourceDatabasePopulator.execute(ds);
  }

  @Override
  protected List<?> userConverters() {
    return Arrays.asList(
            new EnumToStringConverter<Enum<?>>(), new StringToEnumConverter(UpdateLog.Status.class));
  }
}
