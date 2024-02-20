package codes.thischwa.dyndrest;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@Configuration
class DatabaseTestConfig {

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
}
