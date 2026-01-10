package codes.thischwa.dyndrest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** The base config of a zone. */
@EqualsAndHashCode(callSuper = true)
@Data
public class Zone extends AbstractJdbcEntity {

  private String name;

  private String ns;
}
