package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.provider.ProviderException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
class DomainRobotProvider extends GenericProvider implements InitializingBean {

  private final AppConfig appConfig;

  private final DomainRobotConfigurator domainRobotConfigurator;

  private final ZoneClientWrapper zcw;

  DomainRobotProvider(AppConfig appConfig, DomainRobotConfigurator domainRobotConfigurator,
                      ZoneClientWrapper zcw) {
    this.appConfig = appConfig;
    this.domainRobotConfigurator = domainRobotConfigurator;
    this.zcw = zcw;
  }

  @Override
  public void validateHostConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      domainRobotConfigurator.getConfiguredZones().forEach(this::checkZone);
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
      Zone zone = zcw.info(zoneStr, domainRobotConfigurator.getPrimaryNameServer(zoneStr));
      log.info("*** Zone confirmed: {}", zone.getOrigin());
    } catch (ProviderException e) {
      log.error("Error while getting zone info of " + zoneStr, e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
  }

  @Override
  public Set<String> getConfiguredHosts() {
    return domainRobotConfigurator.getConfiguredHosts();
  }

  @Override
  public String getApitoken(String host) {
    return domainRobotConfigurator.getApitoken(host);
  }

  Zone zoneInfo(String host) throws ProviderException, IllegalArgumentException {
    if (!domainRobotConfigurator.hostExists(host)) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    String zone = zcw.deriveZone(host);
    String primaryNameServer = domainRobotConfigurator.getPrimaryNameServer(zone);
    return zcw.info(zone, primaryNameServer);
  }

  @Override
  public void afterPropertiesSet() throws IllegalArgumentException {
    validateHostConfiguration();
  }
}
