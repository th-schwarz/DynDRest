package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Represents an enriched Host entity. */
@EqualsAndHashCode(callSuper = true)
@Data
public class FullHost extends Host {
  private String zone;
  private String ns;

  private LocalDateTime changed;
}
