package codes.thischwa.dyndrest.provider.impl;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.UpdateHookException;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.util.NetUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Generic helper class provider implementations. */
@SuppressWarnings("RedundantThrows")
@Slf4j
public abstract class GenericProvider implements Provider {

  protected final AppConfig appConfig;
  protected final DynamicSecurityChainManager securityChainManager;
  protected final HostZoneService hostZoneService;

  protected GenericProvider(AppConfig appConfig, DynamicSecurityChainManager securityChainManager, HostZoneService hostZoneService) {
    this.appConfig = appConfig;
    this.securityChainManager = securityChainManager;
    this.hostZoneService = hostZoneService;
  }

  @Override
  public void validateHostZoneConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      hostZoneService.getConfiguredZones().forEach(this::confirmZone);
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
    for (HostEnriched host : hosts) {
      securityChainManager.addOrUpdateHost(host);
    }
    log.info("Registered {} hosts for authentication", hosts.size());
  }

  /**
   * Resolves the IP address of the given host via a name server request.<br/>
   * Should be implemented by the deriving class to fetch the real zone data.
   *
   * @param host the host
   * @return the IP setting
   * @throws ProviderException if the resolution fails
   */
  @Override
  public IpSetting info(String host) throws ProviderException {
    return NetUtil.resolve(host);
  }

  /**
   * A wrapper for {@link #update(String, IpSetting)} with respect of hooks before and after.
   *
   * @param host the host
   * @param ipSetting the ip setting
   * @throws ProviderException can be thrown while provider calls
   */
  public final void processUpdate(String host, IpSetting ipSetting) throws ProviderException {
    update(host, ipSetting);
  }
}
