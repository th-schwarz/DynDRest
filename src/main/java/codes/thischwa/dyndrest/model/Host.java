package codes.thischwa.dyndrest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Represents a Host entity. */
@EqualsAndHashCode(callSuper = true)
@Data
public class Host extends AbstractJdbcEntity {

  private String name;

  @EqualsAndHashCode.Exclude private String apiToken;

  @JsonIgnore
  private Integer zoneId;

  /**
   * Creates and initializes a new instance of the Host class, if the desired host is an instance of
   * {@link FullHost}.<br>
   * Required for database processing.
   *
   * @param host the host instance
   * @return A new instance of the Host class, if host is an instance of FullHost.
   */
  public static Host getInstance(Host host) {
    if (!(host instanceof FullHost)) {
      return host;
    }
    Host tmpHost = new Host();
    tmpHost.setId(host.getId());
    tmpHost.setName(host.getName());
    tmpHost.setApiToken(host.getApiToken());
    tmpHost.setZoneId(host.getZoneId());
    tmpHost.setChanged(host.getChanged());
    return tmpHost;
  }
}
