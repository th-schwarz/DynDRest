package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.Data;

/** Represents a Host entity. */
@Data
public class Host {
  private Integer id;

  private String name;
  private String fullHost;
  private String apiToken;

  private Integer zoneId;

  private LocalDateTime changed;
}
