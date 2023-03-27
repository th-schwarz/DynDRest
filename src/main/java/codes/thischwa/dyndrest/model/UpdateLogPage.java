package codes.thischwa.dyndrest.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Data object used by the update log.
 */
@Getter
@Setter
public class UpdateLogPage {

  private int total;

  private int totalPage;

  private int page;

  private int pageSize;

  private List<UpdateItem> items;

}
