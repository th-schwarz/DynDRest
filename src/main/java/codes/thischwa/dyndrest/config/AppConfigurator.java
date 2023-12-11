package codes.thischwa.dyndrest.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Configuration bean for the application configuration.<br>
 * Main task is reading and validating the host configuration.
 */
@Slf4j
@Component
public class AppConfigurator implements InitializingBean {
  private final AppConfig appConfig;

  private Map<String, String> zoneData = new HashMap<>();
  // <fqdn, apitoken>
  private Map<String, String> apitokenData = new HashMap<>();

  public AppConfigurator(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  @Override
  public void afterPropertiesSet() {
    readAndValidate();
    log.info("*** Api-token and zone data are read and validated successful!");
  }

  public Set<String> getConfiguredHosts() {
    return apitokenData.keySet();
  }

  public Set<String> getConfiguredZones() {
    return zoneData.keySet();
  }

  /**
   * Checks if the desired host is configured.
   *
   * @param host the desired host
   * @return true if the host exists in the configuration
   */
  public boolean hostExists(String host) {
    return apitokenData.containsKey(host);
  }

  /**
   * Fetches the api-token of the desired host.
   * @param host the host
   * @return the api-token
   * @throws IllegalArgumentException if the desired host isn't configured
   */
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
    apitokenData.clear();
    zoneData.clear();
    appConfig.zones().forEach(this::readZoneConfig);
  }

  private void readZoneConfig(AppConfig.Zone zone) {
    zoneData.put(zone.name(), zone.ns());
    List<String> hostRawData = zone.hosts();
    if (hostRawData.isEmpty()) {
      throw new IllegalArgumentException("Missing host data for: " + zone.name());
    }
    hostRawData.forEach(hostRaw -> readHostString(hostRaw, zone));
  }

  private void readHostString(String hostRaw, AppConfig.Zone zone) {
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
    if (zoneData.isEmpty() || apitokenData.isEmpty()) {
      throw new IllegalArgumentException("Zone or host data are empty.");
    }
    log.info("*** Configured hosts:");
    apitokenData.keySet().forEach(host -> log.info(" - {}", host));
  }
}
