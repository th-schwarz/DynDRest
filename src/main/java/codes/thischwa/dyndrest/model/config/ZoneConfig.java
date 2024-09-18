package codes.thischwa.dyndrest.model.config;

import codes.thischwa.dyndrest.model.Zone;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** The Configuration of a zone. */
@ConfigurationProperties
public record ZoneConfig(List<Zone> zones) {}
