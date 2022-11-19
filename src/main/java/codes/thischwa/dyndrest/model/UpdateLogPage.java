package codes.thischwa.dyndrest.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UpdateLogPage {

	private @Getter @Setter int total;

	private @Getter @Setter int totalPage;

	private @Getter @Setter int page;

	private @Getter @Setter int pageSize;

	private @Getter @Setter List<UpdateItem> items;

}
