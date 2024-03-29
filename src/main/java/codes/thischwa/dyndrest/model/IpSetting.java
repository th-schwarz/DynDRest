package codes.thischwa.dyndrest.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/** Object to hold the íp settings. */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class IpSetting {

  private @Nullable Inet4Address ipv4;

  private @Nullable Inet6Address ipv6;

  public IpSetting() {}

  /**
   * Instantiates a new Ip setting with strings for each ip type.
   *
   * @param ipv4Str ipv4 str
   * @param ipv6Str ipv6 str
   * @throws UnknownHostException if the ip strings couldn't convert to an {@link InetAddress}
   *     object.
   */
  public IpSetting(@Nullable String ipv4Str, @Nullable String ipv6Str) throws UnknownHostException {
    if (ipv4Str != null) {
      ipv4 = (Inet4Address) InetAddress.getByName(ipv4Str);
    }
    if (ipv6Str != null) {
      ipv6 = (Inet6Address) InetAddress.getByName(ipv6Str);
    }
  }

  /**
   * Instantiates a new Ip setting with a string. The ip type is determined.
   *
   * @param ipStr the ip str
   * @throws UnknownHostException if the ip strings couldn't convert to an {@link InetAddress}
   *     object.
   */
  public IpSetting(String ipStr) throws UnknownHostException {
    InetAddress ip = InetAddress.getByName(ipStr);
    if (ip instanceof Inet4Address ipTmp) {
      this.ipv4 = ipTmp;
    } else {
      this.ipv6 = (Inet6Address) ip;
    }
  }

  /**
   * Instantiates a new Ip setting with {@link Inet4Address} objects.
   *
   * @param ipv4 the ipv 4
   * @param ipv6 the ipv 6
   */
  public IpSetting(@Nullable InetAddress ipv4, @Nullable InetAddress ipv6) {
    if (ipv4 instanceof Inet4Address ip) {
      this.ipv4 = ip;
    }
    if (ipv6 instanceof Inet6Address ip) {
      this.ipv6 = ip;
    }
  }

  @JsonIgnore
  public boolean isNotSet() {
    return ipv4 == null && ipv6 == null;
  }

  @JsonGetter("ipv4")
  public @Nullable String ipv4ToString() {
    return ipv4 == null ? null : ipv4.getHostAddress();
  }

  @JsonGetter("ipv6")
  public @Nullable String ipv6ToString() {
    return ipv6 == null ? null : ipv6.getHostAddress();
  }
}
