package codes.thischwa.dyndrest.model.config;

import java.util.ArrayList;
import java.util.List;

import codes.thischwa.dyndrest.model.HostEnriched;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/**
 * The ZoneImport class represents the base configuration of a zone import. It contains a list of
 * Zone objects, which represent individual zones with their name, name server, and hosts.
 */
@ConfigurationProperties(prefix = "dyndrest")
public record ZoneImportConfig(@Nullable List<ZoneImportConfig.Zone> zones) {

  /**
   * Returns a list of FullHost objects representing the hosts in the zone import configuration.
   *
   * @return A list of FullHost objects
   * @throws IllegalArgumentException if the host entry is not in the correct format
   */
  public List<HostEnriched> getHosts() {
    List<HostEnriched> enrichedHosts = new ArrayList<>();
    if (zones == null) {
      return enrichedHosts;
    }
    for (ZoneImportConfig.Zone zone : zones) {
      for (Host host : zone.hosts()) {
        HostEnriched hostEnriched = new HostEnriched();
        hostEnriched.setName(host.sld());
        hostEnriched.setApiToken(host.apiToken());
        hostEnriched.setZone(zone.name);
        hostEnriched.setNs(zone.ns);
        enrichedHosts.add(hostEnriched);
      }
    }
    return enrichedHosts;
  }

  /**
   * The base config of a zone import.
   *
   * @param name The name / domain of the zone
   * @param ns name server
   * @param hosts host / subdomains
   */
  public record Zone(String name, String ns, List<Host> hosts) {}


  /**
   * Represents a Host entity with an sld (second-level domain) and an apiToken.
   *
   * @param sld       The second-level domain associated with the host.
   * @param apiToken  The API token for authentication or other purposes.
   */
  public record Host(String sld, String apiToken) {}
}
