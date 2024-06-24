package codes.thischwa.dyndrest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/** The base configuration of the application. */
@ConfigurationProperties(prefix = "dyndrest")
public record AppConfig(
    String provider,
    boolean greetingEnabled,
    boolean hostValidationEnabled,
    int updateIpChangedStatus,
    int updateLogPageSize,
    String updateLogDatePattern,
    boolean updateLogPageEnabled,
    @Nullable String updateLogUserName,
    @Nullable String updateLogUserPassword,
    boolean updateLogRestForceHttps,
    @Nullable String healthCheckUserName,
    @Nullable String healthCheckUserPassword,
    Database database) {

  /** Represents a configuration class for a database connection. */
  public record Database(
      String driverClassName,
      String jdbcUrlPrefix,
      String file,
      String user,
      String password,
      String dumpFile,
      @Nullable Backup backup,
      @Nullable Restore restore) {

    /**
     * Represents a backup configuration.
     *
     * @param enabled Specifies if the backup is enabled or not.
     * @param path The path where the backup files will be stored, e.g {@code ./backup}. This should
     *     be a valid file system path.
     * @param cron The cron expression that defines the backup schedule.
     */
    public record Backup(boolean enabled, String path, String cron) {}

    /**
     * Represents a restore configuration.
     *
     * @param enabled Specifies if the restore functionality is enabled or not.
     * @param path The path where the backup files for restore are stored, e.g. {@code ./restore}.
     *     This should be a valid file system path.
     */
    public record Restore(boolean enabled, String path) {}
  }
}
