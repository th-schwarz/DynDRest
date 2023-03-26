package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.client.Domainrobot;
import org.domainrobot.sdk.models.DomainRobotHeaders;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Configuration bean for the provider implementation of domainrobot.
 */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Component
@Slf4j
class DomainRobotConfigurator implements InitializingBean {

  private static final Map<String, String> customHeaders =
      new HashMap<>(Map.of(DomainRobotHeaders.DOMAINROBOT_HEADER_WEBSOCKET, "NONE"));
  private final AppConfig appConfig;
  private final AutoDnsConfig autoDnsConfig;
  private final DomainRobotConfig domainRobotConfig;
  // <zone, ns>
  private Map<String, String> zoneData = null;
  // <fqdn, apitoken>
  private Map<String, String> apitokenData = null;

  public DomainRobotConfigurator(AppConfig appConfig, AutoDnsConfig autoDnsConfig,
                                 DomainRobotConfig domainRobotConfig) {
    this.appConfig = appConfig;
    this.autoDnsConfig = autoDnsConfig;
    this.domainRobotConfig = domainRobotConfig;
  }

  @Bean
  Provider provider() {
    final ZoneClientWrapper zcw = buildZoneClientWrapper();
    return new DomainRobotProvider(appConfig, this, zcw);
  }

  ZoneClientWrapper buildZoneClientWrapper() {
    return new ZoneClientWrapper(
        new Domainrobot(autoDnsConfig.user(), String.valueOf(autoDnsConfig.context()),
            autoDnsConfig.password(),
            autoDnsConfig.url()).getZone(), customHeaders, domainRobotConfig.defaultTtl());
  }

  @Override
  public void afterPropertiesSet() {
    readAndValidate();
    log.info("*** Api-token and zone data are read and validated successful!");
  }

  public int getDefaultTtl() {
    return domainRobotConfig.defaultTtl();
  }

  public Set<String> getConfiguredHosts() {
    return apitokenData.keySet();
  }

  public Set<String> getConfiguredZones() {
    return zoneData.keySet();
  }

  public boolean hostExists(String host) {
    return apitokenData.containsKey(host);
  }

  public String getApitoken(String host) throws IllegalArgumentException {
    if (!hostExists(host)) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    return apitokenData.get(host);
  }

  public String getPrimaryNameServer(String zone) throws IllegalArgumentException {
    if (!zoneData.containsKey(zone)) {
      throw new IllegalArgumentException("Zone isn't configured: " + zone);
    }
    return zoneData.get(zone);
  }


  void readAndValidate() {
    read();
    validate();
  }

  void read() throws IllegalArgumentException {
    apitokenData = new HashMap<>();
    zoneData = new HashMap<>();
    domainRobotConfig.zones().forEach(this::readZoneConfig);
  }

  private void readZoneConfig(DomainRobotConfig.Zone zone) {
    zoneData.put(zone.name(), zone.ns());
    List<String> hostRawData = zone.hosts();
    if (hostRawData == null || hostRawData.isEmpty()) {
      throw new IllegalArgumentException("Missing host data for: " + zone.name());
    }
    hostRawData.forEach(hostRaw -> readHostString(hostRaw, zone));
  }

  private void readHostString(String hostRaw, DomainRobotConfig.Zone zone) {
    String[] parts = hostRaw.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException(
          "The host entry must be in the following format: [sld|:[apitoken], but it was: "
              + hostRaw);
    }
    // build the fqdn hostname
    String host = String.format("%s.%s", parts[0], zone.name());
    apitokenData.put(host, parts[1]);
  }

  void validate() {
    if (zoneData == null || zoneData.isEmpty() || apitokenData == null
        || apitokenData.isEmpty()) {
      throw new IllegalArgumentException("Zone or host data are empty.");
    }
    log.info("*** Configured hosts:");
    apitokenData.keySet().forEach(host -> log.info(" - {}", host));
  }

  // just for testing
  DomainRobotConfig getDomainRobotConfig() {
    return domainRobotConfig;
  }
}
