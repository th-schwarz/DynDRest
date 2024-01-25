package codes.thischwa.dyndrest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents a Host entity. */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Host {
  @jakarta.persistence.Id private Integer id;
  private String name;
  private String apiToken;

  @Column(name = "zone_id")
  private Integer zoneId;

  private LocalDateTime changed;
}
