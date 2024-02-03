package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AbstractJdbcModel is an abstract base class that provides common fields for JDBC models. It
 * includes an id field and a changed field that represent the primary key and last modified
 * timestamp, respectively.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractJdbcModel {

  private Integer id;

  private LocalDateTime changed;
}
