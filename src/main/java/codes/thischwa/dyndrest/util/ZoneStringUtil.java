package codes.thischwa.dyndrest.util;

import codes.thischwa.dyndrest.model.Zone;
import java.net.IDN;
import org.jspecify.annotations.Nullable;

/**
 * Utility class for working with zone strings and fully qualified domain names (FQDN).
 */
public class ZoneStringUtil {

  private ZoneStringUtil() {
  }

  /**
   * Constructs a fully qualified domain name (FQDN) from a second-level domain and zone.
   *
   * @param sld  the second-level domain
   * @param zone the zone
   * @return the fully qualified domain name
   */
  public static String getFqdn(String sld, Zone zone) {
    return sld + "." + zone.getName();
  }

  /**
   * Extracts the zone name from a fully qualified domain name.
   *
   * @param fqdn the fully qualified domain name
   * @return the zone name (everything after the first dot)
   */
  public static String getZoneName(String fqdn) {
    return fqdn.substring(fqdn.indexOf(".") + 1);
  }

  /**
   * Checks whether a host name consists of exactly one valid DNS label.
   *
   * @param hostname the host label to validate
   * @return true if the host name is one valid DNS label
   */
  public static boolean isValidHostLabel(@Nullable String hostname) {
    if (hostname == null || hostname.isBlank()) {
      return false;
    }
    try {
      String asciiHostname = IDN.toASCII(hostname, IDN.USE_STD3_ASCII_RULES);
      return asciiHostname.length() <= 63 && !asciiHostname.contains(".");
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
