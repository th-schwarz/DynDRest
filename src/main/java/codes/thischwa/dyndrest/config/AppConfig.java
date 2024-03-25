package codes.thischwa.dyndrest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/** The base configuration of the application. */
@ConfigurationProperties(prefix = "dyndrest")
public record AppConfig(
    String provider,
    boolean greetingEnabled,
    boolean hostValidationEnabled,
    int updateIpChangedStatus,
    int updateLogPageSize,
    String updateLogDatePattern,
    boolean updateLogPageEnabled,
    @Nullable String updateLogUserName,
    @Nullable String updateLogUserPassword,
    boolean updateLogRestForceHttps,
    @Nullable String healthCheckUserName,
    @Nullable String healthCheckUserPassword,
    @Nullable Database database) {

  /** Represents a configuration class for a database connection. */
  public record Database(
      String driverClassName, String jdbcUrlPrefix, String file, String user, String password) {}
}
