package codes.thischwa.dyndrest.provider.impl.domainrobot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

/**
 * Holds the AutoDNS base url and credentials for the Domainrobot Sdk.
 */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "domainrobot.autodns")
@Getter
@Setter
public class AutoDnsConfig {

	private String url;

	private int context;

	@NotBlank(message = "The user name shouldn't be empty.") private String user;

	@NotBlank(message = "The password shouldn't be empty.") private String password;

}
