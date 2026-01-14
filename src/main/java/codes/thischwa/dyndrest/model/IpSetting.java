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
import org.jspecify.annotations.Nullable;

/** Object to hold the íp settings. */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class IpSetting {

  private @Nullable Inet4Address ipv4;

  private @Nullable Inet6Address ipv6;

  /**
   * Default constructor.
   */
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

  /**
   * Constructs an IpSetting object by determining the type of the provided InetAddress
   * and assigning it to either the IPv4 or IPv6 field.
   *
   * @param remoteIp the remote IP address, which can represent either an IPv4 address
   *                 (Inet4Address) or an IPv6 address (Inet6Address).
   */
  public IpSetting(InetAddress remoteIp) {
    this.ipv4 = remoteIp instanceof Inet4Address ? (Inet4Address) remoteIp : null;
    this.ipv6 = remoteIp instanceof Inet6Address ? (Inet6Address) remoteIp : null;
  }

  /**
   * Checks if neither IPv4 nor IPv6 is set.
   *
   * @return true if both IP addresses are null, false otherwise
   */
  @JsonIgnore
  public boolean isNotSet() {
    return ipv4 == null && ipv6 == null;
  }

  /**
   * Returns the IPv4 address as a string.
   *
   * @return the IPv4 address string, or null if not set
   */
  @JsonGetter("ipv4")
  public @Nullable String ipv4ToString() {
    return ipv4 == null ? null : ipv4.getHostAddress();
  }

  /**
   * Returns the IPv6 address as a string.
   *
   * @return the IPv6 address string, or null if not set
   */
  @JsonGetter("ipv6")
  public @Nullable String ipv6ToString() {
    return ipv6 == null ? null : ipv6.getHostAddress();
  }

  /**
   * Sets the IPv4 address for this instance. The provided value is validated and converted into an
   * {@link Inet4Address} object. If the value is null, the IPv4 address is cleared.
   *
   * @param value the IPv4 address as a string, or null to clear the IPv4 address. If a non-null
   *              value is provided, it must represent a valid IPv4 address.
   * @throws IllegalArgumentException if the provided value is not a valid IPv4 address.
   */
  public void setIpv4(@Nullable String value) {
    if (value == null) {
      this.ipv4 = null;
      return;
    }
    try {
      this.ipv4 = (Inet4Address) InetAddress.getByName(value);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Invalid IPv4 address: " + value, e);
    }
  }

  /**
   * Sets the IPv4 address for this instance.
   *
   * @param ipv4 the IPv4 address to set, or null to clear the current IPv4 address. If provided,
   *             the address must be an instance of {@link Inet4Address}.
   */
  public void setIpv4(@Nullable Inet4Address ipv4) {
    this.ipv4 = ipv4;
  }

  /**
   * Sets the IPv6 address for this instance. The provided value is validated and converted into an
   * {@link Inet6Address} object. If the value is null, the IPv6 address is cleared.
   *
   * @param value the IPv6 address as a string, or null to clear the IPv6 address. If a non-null
   *              value is provided, it must represent a valid IPv6 address.
   * @throws IllegalArgumentException if the provided value is not a valid IPv6 address.
   */
  public void setIpv6(@Nullable String value) {
    if (value == null) {
      this.ipv6 = null;
      return;
    }
    try {
      this.ipv6 = (Inet6Address) InetAddress.getByName(value);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Invalid IPv6 address: " + value, e);
    }
  }

  /**
   * Sets the IPv6 address for this instance. The provided value can be null to clear the current IPv6 address.
   *
   * @param ipv6 the IPv6 address to set, or null to clear the current IPv6 address. If provided, the address must
   *             be an instance of {@link Inet6Address}.
   */
  public void setIpv6(@Nullable Inet6Address ipv6) {
    this.ipv6 = ipv6;
  }
}
