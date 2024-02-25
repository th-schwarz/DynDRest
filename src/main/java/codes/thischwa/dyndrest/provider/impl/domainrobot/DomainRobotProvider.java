package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.service.HostZoneService;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
class DomainRobotProvider extends GenericProvider implements InitializingBean {

  private final AppConfig appConfig;

  private final HostZoneService hostZoneService;

  private final ZoneClientWrapper zcw;

  DomainRobotProvider(AppConfig appConfig, HostZoneService hostZoneService, ZoneClientWrapper zcw) {
    this.appConfig = appConfig;
    this.hostZoneService = hostZoneService;
    this.zcw = zcw;
  }

  @Override
  public void validateHostZoneConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      hostZoneService.getConfiguredZones().forEach(this::checkZone);
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

  private void checkZone(codes.thischwa.dyndrest.model.Zone myZone) throws IllegalArgumentException {
    Zone zone;
    try {
      zone = zcw.info(myZone.getName(), myZone.getNs());
      log.info("*** Zone confirmed: {}", zone.getOrigin());
    } catch (ProviderException e) {
      log.error("Error while getting zone info of " + myZone.getName(), e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
    checkHosts(zone);
  }

  private void checkHosts(Zone zone) throws IllegalArgumentException {
    List<FullHost> hosts = hostZoneService.findHostsOfZone(zone.getOrigin());
    for (FullHost host : hosts) {
      if (zcw.hasSubTld(zone, host.getName())) {
        log.info("Host confirmed: {}", host.getFullHost());
      } else {
        throw new IllegalArgumentException("Host not confirmed: " + host.getFullHost());
      }
    }
  }

  Zone zoneInfo(String host) throws ProviderException, IllegalArgumentException {
    Optional<FullHost> optFullHost = hostZoneService.getHost(host);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    FullHost fullHost = optFullHost.get();
    String zone = fullHost.getZone();
    String primaryNameServer = fullHost.getNs();
    return zcw.info(zone, primaryNameServer);
  }

  @Override
  public void afterPropertiesSet() throws IllegalArgumentException {
    validateHostZoneConfiguration();
  }
}
