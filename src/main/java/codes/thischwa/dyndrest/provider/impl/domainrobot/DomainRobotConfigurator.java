package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.config.AppConfigurator;
import codes.thischwa.dyndrest.provider.Provider;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.client.Domainrobot;
import org.domainrobot.sdk.models.DomainRobotHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Configuration bean for the provider implementation of domainrobot. */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Component
@Slf4j
class DomainRobotConfigurator {

  private static final Map<String, String> customHeaders =
      new HashMap<>(Map.of(DomainRobotHeaders.DOMAINROBOT_HEADER_WEBSOCKET, "NONE"));
  private final AppConfig appConfig;
  private final AppConfigurator appConfigurator;
  private final DomainRobotConfig.Autodns autoDnsConfig;
  private final DomainRobotConfig domainRobotConfig;
  // <zone, ns>
  private final @Nullable Map<String, String> zoneData = null;
  // <fqdn, apitoken>
  private final @Nullable Map<String, String> apitokenData = null;

  public DomainRobotConfigurator(
      AppConfig appConfig, AppConfigurator appConfigurator, DomainRobotConfig domainRobotConfig) {
    this.appConfig = appConfig;
    this.appConfigurator = appConfigurator;
    this.autoDnsConfig = domainRobotConfig.autodns();
    this.domainRobotConfig = domainRobotConfig;
  }

  @Bean
  Provider provider() {
    final ZoneClientWrapper zcw = buildZoneClientWrapper();
    return new DomainRobotProvider(appConfig, appConfigurator, zcw);
  }

  ZoneClientWrapper buildZoneClientWrapper() {
    return new ZoneClientWrapper(
        new Domainrobot(
                autoDnsConfig.user(),
                String.valueOf(autoDnsConfig.context()),
                autoDnsConfig.password(),
                autoDnsConfig.url())
            .getZone(),
        customHeaders,
        domainRobotConfig.defaultTtl());
  }
}
