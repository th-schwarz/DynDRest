package codes.thischwa.dyndrest;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@Configuration
class DatabaseTestConfig extends AbstractJdbcConfiguration {

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
}
