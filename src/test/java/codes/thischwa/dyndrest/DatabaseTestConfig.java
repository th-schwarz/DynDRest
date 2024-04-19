package codes.thischwa.dyndrest;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.model.converter.EnumToStringConverter;
import codes.thischwa.dyndrest.model.converter.StringToEnumConverter;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.transaction.TransactionManager;

@Configuration
class DatabaseTestConfig extends AbstractJdbcConfiguration {

  @Bean
  DataSource getDataSource() {
    return new EmbeddedDatabaseBuilder()
        .setName("dyndrest")
        .setType(H2)
        .setScriptEncoding("UTF-8")
        .ignoreFailedDrops(true)
        .continueOnError(true)
        .addScript("/h2/dump.sql")
        .build();
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
