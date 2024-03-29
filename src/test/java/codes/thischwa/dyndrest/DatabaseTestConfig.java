package codes.thischwa.dyndrest;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import javax.sql.DataSource;

import codes.thischwa.dyndrest.model.converter.EnumToStringConverter;
import codes.thischwa.dyndrest.model.converter.StringToEnumConverter;
import codes.thischwa.dyndrest.model.UpdateLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import java.util.Arrays;
import java.util.List;

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

  @Override
  protected List<?> userConverters() {
    return Arrays.asList(
        new EnumToStringConverter(), new StringToEnumConverter(UpdateLog.Status.class));
  }
}
