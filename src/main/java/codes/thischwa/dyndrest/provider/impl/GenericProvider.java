package codes.thischwa.dyndrest.provider.impl;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import codes.thischwa.dyndrest.util.NetUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic helper class provider implementations.
 */
@SuppressWarnings("RedundantThrows")
@Slf4j
public abstract class GenericProvider implements Provider {

  protected final AppConfig appConfig;
  protected final DynamicSecurityChainManager securityChainManager;
  protected final HostZoneService hostZoneService;
  protected final ZoneUpdaterService zoneUpdaterService;

  /**
   * Constructor for GenericProvider.
   *
   * @param appConfig              the application configuration
   * @param securityChainManager   the security chain manager
   * @param hostZoneService        the host zone service
   * @param zoneUpdaterService     the zone updater service
   */
  protected GenericProvider(AppConfig appConfig, DynamicSecurityChainManager securityChainManager, HostZoneService hostZoneService,
                            ZoneUpdaterService zoneUpdaterService) {
    this.appConfig = appConfig;
    this.securityChainManager = securityChainManager;
    this.hostZoneService = hostZoneService;
    this.zoneUpdaterService = zoneUpdaterService;
  }

  @Override
  public void validateHostZoneConfiguration() throws IllegalArgumentException {
    List<HostEnriched> configuredHosts = hostZoneService.getConfiguredHosts();
    try {
      List<HostInfoHolder> hostInfoHolderList = getCurrentConfiguredHosts(configuredHosts);
      if (appConfig.zoneUpdateSchedulerEnabled()) {
        zoneUpdaterService.addCurrentHosts(hostInfoHolderList.toArray(new HostInfoHolder[0]));
      }
      if (appConfig.hostValidationEnabled()) {
        hostZoneService.getConfiguredZones().forEach(this::confirmZone);
      }
    } catch (ProviderException e) {
      throw new IllegalArgumentException("Error while getting current configured hosts", e);
    }

    // Register all configured hosts for authentication
    registerHostsForAuthentication();
  }

  /**
   * Registers all configured hosts for authentication.
   * Each host will be authenticated with hostname=user and apiToken=password.
   */
  protected void registerHostsForAuthentication() {
    List<HostEnriched> hosts = hostZoneService.getConfiguredHosts();
    securityChainManager.addOrUpdateHost(hosts.toArray(new HostEnriched[0]));
    log.info("Registered {} hosts for authentication", hosts.size());
  }

  /**
   * Resolves the IP address of the given host via a name server request.<br/>
   * Should be implemented by the deriving class to fetch the real zone data.
   *
   * @param fqdn the host
   * @return the IP setting
   * @throws ProviderException if the resolution fails
   */
  @Override
  public IpSetting info(String fqdn) throws ProviderException {
    return NetUtil.resolve(fqdn);
  }
}
