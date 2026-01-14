package codes.thischwa.dyndrest.model.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
    @Nullable String adminUserName,
    @Nullable String adminUserPassword,
    @Nullable Integer zoneUpdateIntervalSeconds
) {

  /**
   * Determines whether the scheduler is enabled based on the presence of the update interval.
   *
   * @return true if an update interval is defined, indicating that the scheduler is enabled;
   *         false otherwise
   */
  public boolean schedulerEnabled() {
    return zoneUpdateIntervalSeconds != null;
  }
}
