package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.service.HostZoneService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** The type Domain robot provider. */
@Slf4j
@ConditionalOnProperty(value = "dyndrest.provider", havingValue = "domainrobot")
class DomainRobotProvider extends GenericProvider implements InitializingBean {

  private final AppConfig appConfig;

  private final HostZoneService hostZoneService;

  private final ZoneClientWrapper zcw;

  /**
   * Instantiates a new Domain robot provider.
   *
   * @param appConfig the app config
   * @param hostZoneService the host zone service
   * @param zcw the zcw
   */
  DomainRobotProvider(AppConfig appConfig, HostZoneService hostZoneService, ZoneClientWrapper zcw) {
    this.appConfig = appConfig;
    this.hostZoneService = hostZoneService;
    this.zcw = zcw;
  }

  @Override
  public void validateHostZoneConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      hostZoneService.getConfiguredZones().forEach(this::zoneConfirmed);
    }
  }

  @Override
  public void update(String host, IpSetting ipSetting) throws ProviderException {
    String sld = host.substring(0, host.indexOf("."));

    // set the IPs in the zone object
    Zone zone = zoneInfo(host);
    if (!zcw.hasIpsChanged(zone, sld, ipSetting)) {
      return;
    }
    zcw.process(zone, sld, ipSetting);

    // processing the update
    zcw.update(zone);
  }

  @Override
  public void addHost(String zoneName, String host) throws ProviderException {
    // not required for domainrobot. #update adds the required records.
  }

  @Override
  public void removeHost(String host) throws ProviderException {
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(host);
    if (!optFullHost.isPresent()) {
      throw new ProviderException("Host isn't configured: " + host);
    }
    HostEnriched hostEnriched = optFullHost.get();
    Zone zone = zoneInfo(host);
    zcw.removeSld(zone, hostEnriched.getName());
    zcw.update(zone);
  }

  private void zoneConfirmed(codes.thischwa.dyndrest.model.Zone myZone)
      throws IllegalArgumentException {
    Zone zone;
    try {
      zone = zcw.info(myZone.getName(), myZone.getNs());
      log.info("*** Zone confirmed: {}", zone.getOrigin());
    } catch (ProviderException e) {
      log.error("Error while getting zone info of " + myZone.getName(), e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
    hostsOfZoneConfirmed(zone);
  }

  private void hostsOfZoneConfirmed(Zone zone) throws IllegalArgumentException {
    Optional<List<HostEnriched>> opt = hostZoneService.findHostsOfZone(zone.getOrigin());
    if (opt.isEmpty()) {
      log.warn("No hosts found for zone: {}", zone.getOrigin());
      return;
    }
    for (HostEnriched host : opt.get()) {
      if (zcw.hasSubTld(zone, host.getName())) {
        log.info("Host confirmed: {}", host.getFullHost());
      } else {
        throw new IllegalArgumentException("Host not confirmed: " + host.getFullHost());
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
  Zone zoneInfo(String host) throws ProviderException, IllegalArgumentException {
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(host);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    HostEnriched hostEnriched = optFullHost.get();
    String zone = hostEnriched.getZone();
    String primaryNameServer = hostEnriched.getNs();
    return zcw.info(zone, primaryNameServer);
  }

  @Override
  public void afterPropertiesSet() throws IllegalArgumentException {
    validateHostZoneConfiguration();
  }
}
