package codes.thischwa.dyndrest.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/** The base configuration of the application. */
@ConfigurationProperties(prefix = "dyndrest")
public record AppConfig(
    String provider,
    boolean greetingEnabled,
    boolean hostValidationEnabled,
    int updateIpChangedStatus,
    String updateLogFilePattern,
    int updateLogPageSize,
    String updateLogPattern,
    String updateLogDatePattern,
    boolean updateLogPageEnabled,
    @Nullable String updateLogUserName,
    @Nullable String updateLogUserPassword,
    boolean updateLogRestForceHttps,
    String updateLogEncoderPattern,
    @Nullable String healthCheckUserName,
    @Nullable String healthCheckUserPassword,
    List<Zone> zones) {

  /** The Configuration of a zone. */
  public record Zone(
      @NotBlank(message = "The name of the zone shouldn't be empty.") String name,
      @NotBlank(message = "The primary name server of the zone shouldn't be empty.") String ns,
      @NotEmpty(message = "The hosts of the zone shouldn't be empty.") List<String> hosts) {}
}
