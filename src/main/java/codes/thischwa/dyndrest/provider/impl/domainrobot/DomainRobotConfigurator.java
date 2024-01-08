package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.service.ZoneService;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.client.Domainrobot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/** Configuration bean for the provider implementation of domainrobot. */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Component
@Slf4j
class DomainRobotConfigurator {

  private final AppConfig appConfig;
  private final ZoneService zoneService;
  private final DomainRobotConfig.Autodns autoDnsConfig;
  private final DomainRobotConfig domainRobotConfig;

  public DomainRobotConfigurator(
          AppConfig appConfig, ZoneService zoneService, DomainRobotConfig domainRobotConfig) {
    this.appConfig = appConfig;
    this.zoneService = zoneService;
    this.autoDnsConfig = domainRobotConfig.autodns();
    this.domainRobotConfig = domainRobotConfig;
  }

  @Bean
  Provider provider() {
    final ZoneClientWrapper zcw = buildZoneClientWrapper();
    return new DomainRobotProvider(appConfig, zoneService, zcw);
  }

  ZoneClientWrapper buildZoneClientWrapper() {
    return new ZoneClientWrapper(
        new Domainrobot(
                autoDnsConfig.user(),
                String.valueOf(autoDnsConfig.context()),
                autoDnsConfig.password(),
                autoDnsConfig.url())
            .getZone(),
        domainRobotConfig.customHeader(),
        domainRobotConfig.defaultTtl());
  }
}
