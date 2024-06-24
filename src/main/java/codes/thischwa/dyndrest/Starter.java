package codes.thischwa.dyndrest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** The starter of the application. */
@Slf4j
@SpringBootApplication
@EnableJdbcRepositories
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class Starter {

  /**
   * Common main method to start the application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    try {
      SpringApplication.run(Starter.class, args);
    } catch (Exception e) {
      log.error("Unexpected exception, Spring Boot stops! Message: {}", e.getMessage());
      System.exit(10);
    }
  }
}
