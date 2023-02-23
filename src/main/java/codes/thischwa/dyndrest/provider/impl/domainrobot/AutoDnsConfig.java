package codes.thischwa.dyndrest.provider.impl.domainrobot;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds the AutoDNS base url and credentials for the Domainrobot Sdk.
 */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Configuration
@ConfigurationProperties(prefix = "domainrobot.autodns")
@Getter
@Setter
class AutoDnsConfig {

	private String url;

	private int context;

	@NotBlank(message = "The user name shouldn't be empty.") private String user;

	@NotBlank(message = "The password shouldn't be empty.") private String password;

}
