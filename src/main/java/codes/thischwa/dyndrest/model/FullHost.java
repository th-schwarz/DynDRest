package codes.thischwa.dyndrest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Represents an enriched Host entity. */
@EqualsAndHashCode(callSuper = true)
@Data
public class FullHost extends Host {
  @EqualsAndHashCode.Exclude private String zone;
  @EqualsAndHashCode.Exclude private String ns;

  public String getFullHost() {
    return String.format("%s.%s", getName(), zone);
  }
}
