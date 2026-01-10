package codes.thischwa.dyndrest.server.config;

/**
 * Contains all role constants used for security configuration.
 */
public final class Roles {
  public static final String ROLE_ADMIN = "ADMIN";
  public static final String ROLE_LOGVIEWER = "LOGVIEWER";
  public static final String ROLE_USER = "USER";
  public static final String ROLE_HEALTH = "HEALTH";
  public static final String ROLE_HOST = "HOST";

  private Roles() {
    // Utility class - no instantiation
  }
}
