package codes.thischwa.dyndrest.model.config.database;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Represents a backup configuration.
 *
 * @param enabled Enabled backup.
 * @param path The path where the backup files will be stored, e.g {@code ./backup}. This should be
 *     a valid file system path.
 * @param cron The cron expression that defines the backup schedule.
 */
@ConfigurationProperties(prefix = "dyndrest.database.backup")
public record DatabaseBackupConfig(boolean enabled, String path, String cron) {}
