package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

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
  private @Nullable String zone;
  private @Nullable String ns;

  private LocalDateTime changed;
}
