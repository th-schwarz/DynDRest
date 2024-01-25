package codes.thischwa.dyndrest;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@Configuration
@EnableJdbcRepositories
public class DatabaseTestConfig {

  @Bean
  public DataSource getDataSource() {
    return new EmbeddedDatabaseBuilder(new PathMatchingResourcePatternResolver())
        .generateUniqueName(true)
        .setType(H2)
        .setScriptEncoding("UTF-8")
        .ignoreFailedDrops(true)
        //.addScript("h2/dump.sql")
            .addScript("file:target/test-classes/h2/dump.sql")
        .build();
  }
}
