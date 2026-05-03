package codes.thischwa.dyndrest;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for a Liquibase database. It insures that liquibase is executed before the tests!
 */
@Configuration
public class LiquidBaseConf {

  @Bean
  public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
    liquibase.setContexts("test");
    liquibase.setDropFirst(true);
    return liquibase;
  }
}
