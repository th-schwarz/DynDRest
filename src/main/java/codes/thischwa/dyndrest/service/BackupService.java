package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.config.AppConfig;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The BackupService class is responsible for performing database backups based on the provided
 * configuration.<br>
 * Hint: The service is specific for h2!
 */
@Service
@EnableScheduling
@Profile("!test")
@Slf4j
@ConditionalOnProperty(name = "dyndrest.database.backup.enabled")
public class BackupService {

  private final JdbcTemplate jdbcTemplate;

  private final boolean enabled;
  private final AppConfig.Database database;
  private final AppConfig.Database.Backup backup;

  @Nullable
  private String dumpPath;

  /**
   * The BackupService class is responsible for performing database backups based on the provided
   * configuration.
   */
  public BackupService(AppConfig appConfig, JdbcTemplate jdbcTemplate) {
    assert appConfig.database().backup() != null;
    database = appConfig.database();
    this.backup = appConfig.database().backup();
    this.jdbcTemplate = jdbcTemplate;
    enabled = backup.enabled();
  }

  @PostConstruct
  void init() {
    if (enabled) {
      log.info("Enabled with cron: {}, at {}", backup.cron(), backup.path());
    } else {
      log.info("Disabled");
      return;
    }
    Path backupPath = Paths.get(backup.path()).normalize();
    if (!Files.exists(backupPath)) {
      try {
        Files.createDirectories(backupPath);
        log.info("Backup directory successful created: {}", backupPath.toAbsolutePath());
      } catch (IOException e) {
        throw new RuntimeException("couldn't create backup path: " + backupPath.toAbsolutePath());
      }
    }
    dumpPath = Paths.get(backupPath.toString(), database.dumpFile()).toString();
  }

  /**
   * This method is responsible for performing the database backup process. It is scheduled to run
   * at regular intervals based on the cron expression specified in the configuration.
   */
  @Transactional
  @Scheduled(cron = "${dyndrest.database.backup.cron}")
  public void process() {
    if (!enabled) {
      return;
    }
    String sql = String.format("SCRIPT DROP TO '%s';", dumpPath);
    jdbcTemplate.execute(sql);
    log.info("Database backup successful processed: {}", dumpPath);
  }
}
