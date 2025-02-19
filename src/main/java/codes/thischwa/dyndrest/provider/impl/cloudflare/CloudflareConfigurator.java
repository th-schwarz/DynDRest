package codes.thischwa.dyndrest.provider.impl.cloudflare;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.service.HostZoneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "cloudflare")
@Component
@Slf4j
public class CloudflareConfigurator {

  private final AppConfig appConfig;
  private final CloudflareConfig config;
  private final HostZoneService hostZoneService;

  public CloudflareConfigurator(AppConfig appConfig, CloudflareConfig config, HostZoneService hostZoneService) {
    this.appConfig = appConfig;
    this.config = config;
    this.hostZoneService = hostZoneService;
  }

  @Bean
  Provider provider() {
    return new CloudflareProvider(appConfig, config, hostZoneService);
  }
}
