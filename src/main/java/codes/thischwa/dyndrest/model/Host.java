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

  /**
   * Creates and initializes a new instance of the Host class.
   *
   * @param name     The name of the host.
   * @param apiToken The API token of the host.
   * @param zoneId   The zone ID of the host.
   * @param changed  The date and time the host was last changed.
   * @return A new instance of the Host class.
   */
  public static Host getInstance(
      String name, String apiToken, Integer zoneId, LocalDateTime changed) {
    Host host = new Host();
    host.setName(name);
    host.setApiToken(apiToken);
    host.setZoneId(zoneId);
    host.setChanged(changed);
    return host;
  }
}
