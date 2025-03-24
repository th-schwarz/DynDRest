package codes.thischwa.dyndrest.util;

import codes.thischwa.dyndrest.model.IpSetting;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import codes.thischwa.dyndrest.provider.impl.cloudflare.CloudflareProvider;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/** Some network relevant utils. */
public class NetUtil {

  private NetUtil() {
    Lookup.getDefaultCache(DClass.IN).setMaxEntries(0);
  }

  public static boolean isIp(String ipStr) {
    return NetUtil.isIpv4(ipStr) || NetUtil.isIpv6(ipStr);
  }

  /**
   * Checks if the desired ip String is IPv4.
   *
   * @param ipStr the ip str
   * @return true if the ip string is IPv4, otherwise false
   */
  static boolean isIpv4(String ipStr) {
    try {
      return (InetAddress.getByName(ipStr) instanceof Inet4Address);
    } catch (UnknownHostException e) {
      return false;
    }
  }

  /**
   * Checks if the desired ip String is IPvl6.
   *
   * @param ipStr the ip str
   * @return true if the ip string is IPv6, otherwise false
   */
  static boolean isIpv6(String ipStr) {
    try {
      return (InetAddress.getByName(ipStr) instanceof Inet6Address);
    } catch (UnknownHostException e) {
      return false;
    }
  }

  /**
   * Build a basic auth string for the desired user and password.
   *
   * @param user the user
   * @param pwd the pwd
   * @return the basic auth string
   */
  static String buildBasicAuth(String user, String pwd) {
    String authStr = String.format("%s:%s", user, pwd);
    String base64Creds =
        Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));
    return "Basic " + base64Creds;
  }

  /**
   * Fetches the base url from spring {@link ServletUriComponentsBuilder#fromCurrentContextPath()}
   * and fores https if desired.
   *
   * @param forceHttps the force https
   * @return the base url
   */
  public static String getBaseUrl(boolean forceHttps) {
    ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
    if (forceHttps) {
      builder.scheme("https");
    }
    return builder.replacePath(null).build().toUriString();
  }

  /**
   * Resolves the ip settings of the desired 'hostName'.
   *
   * @param hostName the host name
   * @return the ip setting
   * @throws IOException if the resolving fails
   */
  public static IpSetting resolve(String hostName) throws IOException {
    IpSetting ipSetting = new IpSetting();
    Record rec = lookup(hostName, Type.A);
    if (rec != null) {
      ipSetting.setIpv4((Inet4Address) ((ARecord) rec).getAddress());
    }

    rec = lookup(hostName, Type.AAAA);
    if (rec != null) {
      ipSetting.setIpv6((Inet6Address) ((AAAARecord) rec).getAddress());
    }
    return ipSetting;
  }

  @Nullable
  private static org.xbill.DNS.Record lookup(String hostName, int type) throws IOException {
    try {
      org.xbill.DNS.Record[] records = new Lookup(hostName, type).run();
      return (records == null || records.length == 0) ? null : records[0];
    } catch (TextParseException e) {
      throw new IOException(String.format("Couldn't lookup for host %s", hostName), e);
    }
  }

  /**
   * Compares two IP addresses for equality.
   *
   * @param ip1 the first IP address to compare, in string format
   * @param ip2 the second IP address to compare, in string format
   * @return true if both IP addresses are equal, false otherwise
   * @throws IllegalArgumentException if either of the IP addresses is invalid
   */
  public static boolean ipEquals(String ip1, String ip2) {
    try {
      InetAddress a1 = InetAddress.getByName(ip1);
      InetAddress a2 = InetAddress.getByName(ip2);
      return a1.equals(a2);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Invalid IP address(es).", e);
    }
  }
}
