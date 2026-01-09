package codes.thischwa.dyndrest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Represents an enriched Host entity. */
@EqualsAndHashCode(callSuper = true)
@Data
public class HostEnriched extends Host {
  @EqualsAndHashCode.Exclude private String zone;
  @EqualsAndHashCode.Exclude private String ns;

  public String getFullHost() {
    return String.format("%s.%s", getSld(), zone);
  }

  public void assignValuesOf(HostEnriched hostEnriched) {
    setZoneId(hostEnriched.getZoneId());
    setZone(hostEnriched.getZone());
    setNs(hostEnriched.getNs());
    setSld(hostEnriched.getSld());
  }
}
