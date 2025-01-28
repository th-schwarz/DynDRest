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

}
