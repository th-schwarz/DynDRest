package codes.thischwa.dyndrest.provider.impl.domainrobot;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds the AutoDNS base url and credentials for the Domainrobot Sdk.
 */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@ConfigurationProperties(prefix = "domainrobot.autodns")
record AutoDnsConfig(String url, int context, @NotBlank(message = "The user name shouldn't be empty.") String user,
					 @NotBlank(message = "The password shouldn't be empty.") String password) {
}