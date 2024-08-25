package codes.thischwa.dyndrest.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/**
 * The ZoneImport class represents the base configuration of a zone import. It contains a list of
 * Zone objects, which represent individual zones with their name, name server, and hosts.
 */
@ConfigurationProperties
public record ZoneImport(@Nullable List<ZoneImport.Zone> zones) {

  /**
   * Returns a list of FullHost objects representing the hosts in the zone import configuration.
   *
   * @return A list of FullHost objects
   * @throws IllegalArgumentException if the host entry is not in the correct format
   */
  public List<FullHost> getHosts() {
    List<FullHost> fullHosts = new ArrayList<>();
    if (zones == null) {
      return fullHosts;
    }
    for (ZoneImport.Zone zone : zones) {
      for (String hostRaw : zone.hosts()) {
        String[] parts = hostRaw.split(":");
        if (parts.length != 2) {
          throw new IllegalArgumentException(
              "The host entry must be in the following format: [sld|:[apiToken], but it was: "
                  + hostRaw);
        }
        FullHost fullHost = new FullHost();
        fullHost.setName(parts[0]);
        fullHost.setApiToken(parts[1]);
        fullHost.setZone(zone.name);
        fullHost.setNs(zone.ns);
        fullHosts.add(fullHost);
      }
    }
    return fullHosts;
  }

  /**
   * The base config of a zone import.
   *
   * @param name The name / domain of the zone
   * @param ns name server
   * @param hosts host / subdomains
   */
  public record Zone(String name, String ns, List<String> hosts) {}
}
