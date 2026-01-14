package codes.thischwa.dyndrest.util;

import codes.thischwa.dyndrest.model.Zone;

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
}
