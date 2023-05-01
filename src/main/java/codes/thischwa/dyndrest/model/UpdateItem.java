package codes.thischwa.dyndrest.model;

import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * The Update item for the {@link UpdateLogPage}.
 */
public record UpdateItem(String dateTime, String host, String ipv4, String ipv6) implements
    Comparable<UpdateItem> {

  /**
   * Instantiates a new Update item.
   *
   * @param dateTime the date time
   * @param host     the host
   * @param ipv4     the ipv4
   * @param ipv6     the ipv6
   */
  public UpdateItem(String dateTime, String host, @Nullable String ipv4, @Nullable String ipv6) {
    this.dateTime = dateTime;
    this.host = host;
    this.ipv4 = ipv4 == null ? "n/a" : ipv4;
    this.ipv6 = ipv6 == null ? "n/a" : ipv6;
  }

  @Override
  public String toString() {
    return "UpdateItem [dateTime=" + dateTime + ", host=" + host + ", ipv4=" + ipv4 + ", ipv6="
        + ipv6 + "]";
  }

  @Override
  public int compareTo(UpdateItem o2) {
    return dateTime.compareTo(o2.dateTime());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UpdateItem other)) {
      return false;
    }
    return Objects.equals(dateTime, other.dateTime) && Objects.equals(host, other.host)
        && Objects.equals(ipv4, other.ipv4)
        && Objects.equals(ipv6, other.ipv6);
  }

}