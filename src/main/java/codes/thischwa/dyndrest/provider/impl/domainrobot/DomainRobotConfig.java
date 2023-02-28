package codes.thischwa.dyndrest.provider.impl.domainrobot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@ConfigurationProperties(prefix = "domainrobot")
record DomainRobotConfig(int defaultTtl, List<Zone> zones) {
		record Zone (
		@NotBlank(message = "The name of the zone shouldn't be empty.") String name,

		@NotBlank(message = "The primary name server of the zone shouldn't be empty.") String ns,

		@NotEmpty(message = "The hosts of the zone shouldn't be empty.") List<String> hosts) {}
}
