package codes.thischwa.dyndrest.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** The Configuration of a zone. */
@ConfigurationProperties
public record ZoneConfig(List<Zone> zones) {

  /**
   * The base config of a zone.
   *
   * @param name The name / domain of the zone
   * @param ns name server
   * @param hosts host / subdomains
   */
  public record Zone(
      @NotBlank(message = "The name of the zone shouldn't be empty.") String name,
      @NotBlank(message = "The primary name server of the zone shouldn't be empty.") String ns,
      @NotEmpty(message = "The hosts of the zone shouldn't be empty.") List<String> hosts) {}
}
