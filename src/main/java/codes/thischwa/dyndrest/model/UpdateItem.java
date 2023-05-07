package codes.thischwa.dyndrest.model;

import org.springframework.lang.Nullable;

/** The Update item for the {@link UpdateLogPage}. */
public record UpdateItem(String dateTime, String host, @Nullable String ipv4, @Nullable String ipv6)
    implements Comparable<UpdateItem> {

  public UpdateItem {
    ipv4 = ipv4 == null ? "n/a" : ipv4;
    ipv6 = ipv6 == null ? "n/a" : ipv6;
  }

  @Override
  public int compareTo(UpdateItem o2) {
    return dateTime.compareTo(o2.dateTime());
  }
}
