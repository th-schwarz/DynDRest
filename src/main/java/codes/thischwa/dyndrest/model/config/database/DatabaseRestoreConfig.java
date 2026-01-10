package codes.thischwa.dyndrest.model.config.database;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Represents a restore configuration.
 *
 * @param enabled Enables the restore.
 * @param path The path where the backup files for restore are stored, e.g. {@code ./restore}. This
 *     should be a valid file system path.
 */
@ConfigurationProperties(prefix = "dyndrest.database.restore")
public record DatabaseRestoreConfig(boolean enabled, String path) {}
