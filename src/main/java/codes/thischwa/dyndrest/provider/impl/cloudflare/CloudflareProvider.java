package codes.thischwa.dyndrest.provider.impl.cloudflare;

import codes.thischwa.cf.CfDnsClient;
import codes.thischwa.cf.CfDnsUtils;
import codes.thischwa.cf.CloudflareApiException;
import codes.thischwa.cf.model.ZoneEntity;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.service.HostZoneService;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
public class CloudflareProvider extends GenericProvider implements InitializingBean {

  private final AppConfig appConfig;
  private final HostZoneService hostZoneService;
  private final CfDnsClient cfDnsClient;

  public CloudflareProvider(
      AppConfig appConfig, CloudflareConfig config, HostZoneService hostZoneService) {
    this.appConfig = appConfig;
    this.hostZoneService = hostZoneService;
    cfDnsClient =
        new CfDnsClient(config.baseUrl(), config.email(), config.apiKey(), config.apiToken());
  }

  @Override
  public void validateHostZoneConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      hostZoneService.getConfiguredZones().forEach(this::zoneConfirmed);
    }
  }

  @Override
  public void update(String host, IpSetting ipSetting) throws ProviderException {
    ZoneEntity zone = fetchZoneFromHost(host);
    try {
      boolean updated = CfDnsUtils.updateHost(cfDnsClient, zone, host, ipSetting.ipv4ToString(), ipSetting.ipv6ToString());
      if (!updated) {
        log.info("*** No update required for host: {}", host);
      }
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public void addHost(String zoneName, String host) throws ProviderException {}

  @Override
  public void removeHost(String host) throws ProviderException {}

  private void zoneConfirmed(Zone myZone) throws IllegalArgumentException {
    ZoneEntity zone;
    try {
      zone = cfDnsClient.getZone(myZone.getName());
      log.info("*** Zone confirmed: {}", zone.getName());
      hostsOfZoneConfirmed(zone);
    } catch (CloudflareApiException | ProviderException e) {
      log.error("Error while getting zone info of {}", myZone.getName(), e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
  }

  private void hostsOfZoneConfirmed(ZoneEntity zone)
      throws IllegalArgumentException, ProviderException {
    Optional<List<HostEnriched>> opt = hostZoneService.findHostsOfZone(zone.getName());
    if (opt.isEmpty()) {
      log.warn("No hosts found for zone: {}", zone.getName());
      return;
    }
    for (HostEnriched host : opt.get()) {
      try {
        if (CfDnsUtils.hasSubTld(cfDnsClient, zone, host.getFullHost())) {
          log.info("Host confirmed: {}", host.getFullHost());
        } else {
          throw new IllegalArgumentException("Host not confirmed: " + host.getFullHost());
        }
      } catch (CloudflareApiException e) {
        throw new ProviderException(e);
      }
    }
  }

  /**
   * Zone info zone.
   *
   * @param host the host
   * @return the zone
   * @throws ProviderException the provider exception
   * @throws IllegalArgumentException the illegal argument exception
   */
  ZoneEntity fetchZoneFromHost(String host) throws ProviderException, IllegalArgumentException {
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(host);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    HostEnriched hostEnriched = optFullHost.get();
    String zone = hostEnriched.getZone();
    try {
      return cfDnsClient.getZone(zone);
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    validateHostZoneConfiguration();
  }
}
