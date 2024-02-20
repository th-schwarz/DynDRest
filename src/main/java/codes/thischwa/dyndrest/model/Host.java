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
