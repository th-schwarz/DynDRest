package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Represents a Host entity. */
@EqualsAndHashCode(callSuper = true)
@Data
public class Host extends AbstractJdbcEntity {

  private String name;
  private String apiToken;

  private Integer zoneId;

  public Host() {}

  /**
   * Creates and initializes a new instance of the Host class.
   *
   * @param name     the name of the host
   * @param apiToken the API token of the host
   * @param zoneId   the zone ID of the host
   * @param changed  the last modified timestamp of the host
   */
  public Host(String name, String apiToken, Integer zoneId, LocalDateTime changed) {
    this.name = name;
    this.apiToken = apiToken;
    this.zoneId = zoneId;
    this.setChanged(changed);
  }

  /**
   * Creates and initializes a new instance of the Host class.
   *
   * @param host the host instance
   * @return A new instance of the Host class, if host is an instance of FullHost.
   */
  public static Host getInstance(Host host) {
    if (!(host instanceof FullHost)) {
      return host;
    }
    Host tmpHost = new Host();
    tmpHost.setName(host.getName());
    tmpHost.setApiToken(host.getApiToken());
    tmpHost.setZoneId(host.getZoneId());
    tmpHost.setChanged(host.getChanged());
    return tmpHost;
  }
}
