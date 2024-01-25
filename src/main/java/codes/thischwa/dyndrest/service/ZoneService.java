package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.config.ZoneConfig;
import codes.thischwa.dyndrest.model.Zone;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * Service to read, validate and hold the zones and hosts data.
 */
@Slf4j
@Service
public class ZoneService implements InitializingBean {

  private final ZoneConfig zoneConfig;

  private final Map<String, String> zoneData = new HashMap<>();
  // <fqdn, apitoken>
  private final Map<String, String> apitokenData = new HashMap<>();

  public ZoneService(ZoneConfig zoneConfig) {
    this.zoneConfig = zoneConfig;
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
   *
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

  /**
   * Gets primary name server for the desired zone.
   *
   * @param zone the desired zone
   * @return the primary name server
   * @throws IllegalArgumentException if the desired zone isn't configured
   */
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
    zoneConfig.zones().forEach(this::readZoneConfig);
  }

  private void readZoneConfig(Zone zone) {
    zoneData.put(zone.name(), zone.ns());
    List<String> hostRawData = zone.hosts();
    if (hostRawData.isEmpty()) {
      throw new IllegalArgumentException("Missing host data for: " + zone.name());
    }
    hostRawData.forEach(hostRaw -> readHostString(hostRaw, zone));
  }

  private void readHostString(String hostRaw, Zone zone) {
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
