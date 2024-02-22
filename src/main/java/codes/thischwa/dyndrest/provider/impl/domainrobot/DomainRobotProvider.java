package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.service.HostZoneService;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;
import org.springframework.beans.factory.InitializingBean;

import java.util.Optional;

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
  public void validateHostConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      hostZoneService.getConfiguredHosts().forEach(this::checkZone);
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

  private void checkZone(FullHost fullHost) throws IllegalArgumentException {
    try {
      Zone zone = zcw.info(fullHost.getZone(), fullHost.getNs());
      log.info("*** Zone confirmed: {}", zone.getOrigin());
    } catch (ProviderException e) {
      log.error("Error while getting zone info of " + fullHost.getZone(), e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
  }

  Zone zoneInfo(String host) throws ProviderException, IllegalArgumentException {
    Optional<FullHost> optFullHost = hostZoneService.getHost(host);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    FullHost fullHost = hostZoneService.getHost(host).get();
    String zone = fullHost.getZone();
    String primaryNameServer = fullHost.getNs();
    return zcw.info(zone, primaryNameServer);
  }

  @Override
  public void afterPropertiesSet() throws IllegalArgumentException {
    validateHostConfiguration();
  }
}
