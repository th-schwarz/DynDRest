package codes.thischwa.dyndrest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class represents a FullUpdateLog, which is a subclass of the UpdateLog class.
 * It contains an additional field called 'host'.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FullUpdateLog extends UpdateLog {

  private String host;
}
