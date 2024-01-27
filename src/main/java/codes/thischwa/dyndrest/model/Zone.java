package codes.thischwa.dyndrest.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The base config of a zone. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Zone {

  @NotBlank(message = "The name of the zone shouldn't be empty.")
  private String name;

  @NotBlank(message = "The primary name server of the zone shouldn't be empty.")
  private String ns;
}
