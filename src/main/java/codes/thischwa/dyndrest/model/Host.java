package codes.thischwa.dyndrest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Represents a Host entity. */
@EqualsAndHashCode(callSuper = true)
@Data
public class Host extends AbstractJdbcModel {

  private String name;
  private String fullHost;
  private String apiToken;

  private Integer zoneId;

}
