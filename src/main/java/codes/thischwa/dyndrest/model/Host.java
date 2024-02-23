package codes.thischwa.dyndrest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Represents a Host entity. */
@EqualsAndHashCode(callSuper = true)
@Data
public class Host extends AbstractJdbcEntity {

  private String name;
  private String apiToken;

  private Integer zoneId;

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
