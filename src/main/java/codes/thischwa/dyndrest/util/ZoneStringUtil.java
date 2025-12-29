package codes.thischwa.dyndrest.util;

import codes.thischwa.dyndrest.model.Zone;

public class ZoneStringUtil {

  private ZoneStringUtil() {
  }

  public static String getFqdn(String sld, Zone zone) {
    return sld + "." + zone.getName();
  }

  public static String getZoneName(String fqdn) {
    return fqdn.substring(fqdn.indexOf(".") + 1);
  }
}
