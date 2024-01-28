package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents a Host entity. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Host {
  private Integer id;

  private String name;
  private String fullHost;
  private String apiToken;

  private Integer zoneId;

  private LocalDateTime changed;
}
