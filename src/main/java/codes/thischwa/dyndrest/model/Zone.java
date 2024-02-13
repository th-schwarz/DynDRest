package codes.thischwa.dyndrest.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** The base config of a zone. */
@EqualsAndHashCode(callSuper = true)
@Data
public class Zone extends AbstractJdbcModel {

  @NotBlank(message = "The name of the zone shouldn't be empty.")
  private String name;

  @NotBlank(message = "The primary name server of the zone shouldn't be empty.")
  private String ns;
}
