package codes.thischwa.dyndrest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;

/**
 * AbstractJdbcModel is an abstract base class that provides common fields for JDBC models. It
 * includes an id field and a changed field that represent the primary key and last modified
 * timestamp, respectively.
 */
@Data
public abstract class AbstractJdbcEntity {

  @Id @Nullable @JsonIgnore
  private Integer id;

  private LocalDateTime changed;
}
