package codes.thischwa.dyndrest.provider.impl.domainrobot;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Configuration
@ConfigurationProperties(prefix = "domainrobot")
class DomainRobotConfig {

	private @Getter @Setter int defaultTtl;

	private @Getter @Setter List<Zone> zones;

	static class Zone {

		@NotBlank(message = "The name of the zone shouldn't be empty.") private @Getter @Setter String name;

		@NotBlank(message = "The primary name server of the zone shouldn't be empty.") private @Getter @Setter String ns;

		// is validated by DDAutoContext#readData
		private @Getter List<String> hosts;

		public void setHosts(List<String> host) {
			this.hosts = host;
		}

	}
}
