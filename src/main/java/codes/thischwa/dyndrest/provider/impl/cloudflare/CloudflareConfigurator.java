package codes.thischwa.dyndrest.provider.impl.cloudflare;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Configurator for the Cloudflare provider.
 * This class is conditionally loaded when the provider is set to "cloudflare".
 */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "cloudflare")
@Component
@Slf4j
public class CloudflareConfigurator {

  private final AppConfig appConfig;
  private final CloudflareConfig config;
  private final HostZoneService hostZoneService;
  private final DynamicSecurityChainManager securityChainManager;
  private final ZoneUpdaterService zoneUpdaterService;

  /**
   * Constructor for the CloudflareConfigurator.
   *
   * @param appConfig            the application configuration
   * @param config               the Cloudflare-specific configuration
   * @param hostZoneService      the service for maintaining hosts and zones
   * @param securityChainManager the security chain manager
   * @param zoneUpdaterService   the service for managing zone updates
   */
  public CloudflareConfigurator(AppConfig appConfig, CloudflareConfig config,
                                HostZoneService hostZoneService, DynamicSecurityChainManager securityChainManager,
                                ZoneUpdaterService zoneUpdaterService) {
    this.appConfig = appConfig;
    this.config = config;
    this.hostZoneService = hostZoneService;
    this.securityChainManager = securityChainManager;
    this.zoneUpdaterService = zoneUpdaterService;
  }

  /**
   * Creates and configures the Cloudflare provider bean.
   *
   * @return the configured Cloudflare provider instance
   */
  @Bean
  Provider provider() {
    return new CloudflareProvider(appConfig, config, hostZoneService, zoneUpdaterService, securityChainManager);
  }
}
