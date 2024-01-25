package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.service.ZoneService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
class DomainRobotProvider extends GenericProvider implements InitializingBean {

  private final AppConfig appConfig;

  private final ZoneService zoneService;

  private final ZoneClientWrapper zcw;

  DomainRobotProvider(AppConfig appConfig, ZoneService zoneService, ZoneClientWrapper zcw) {
    this.appConfig = appConfig;
    this.zoneService = zoneService;
    this.zcw = zcw;
  }

  @Override
  public void validateHostConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      zoneService.getConfiguredZones().forEach(this::checkZone);
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

  private void checkZone(String zoneStr) throws IllegalArgumentException {
    try {
      Zone zone = zcw.info(zoneStr, zoneService.getPrimaryNameServer(zoneStr));
      log.info("*** Zone confirmed: {}", zone.getOrigin());
    } catch (ProviderException e) {
      log.error("Error while getting zone info of " + zoneStr, e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
  }

  @Override
  public Set<String> getConfiguredHosts() {
    return zoneService.getConfiguredHosts();
  }

  @Override
  public String getApitoken(String host) {
    return zoneService.getApitoken(host);
  }

  Zone zoneInfo(String host) throws ProviderException, IllegalArgumentException {
    if (!zoneService.hostExists(host)) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    String zone = zcw.deriveZone(host);
    String primaryNameServer = zoneService.getPrimaryNameServer(zone);
    return zcw.info(zone, primaryNameServer);
  }

  @Override
  public void afterPropertiesSet() throws IllegalArgumentException {
    validateHostConfiguration();
  }
}
