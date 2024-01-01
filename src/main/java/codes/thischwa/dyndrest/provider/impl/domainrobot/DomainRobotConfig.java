package codes.thischwa.dyndrest.provider.impl.domainrobot;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@ConfigurationProperties(prefix = "domainrobot")
record DomainRobotConfig(int defaultTtl, Autodns autodns, Map<String, String> customHeader) {
  record Autodns(
      String url,
      int context,
      @NotBlank(message = "The user name shouldn't be empty.") String user,
      @NotBlank(message = "The password shouldn't be empty.") String password) {}
}
