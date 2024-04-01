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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The ZoneUpdaterService class is responsible for updating zones on a schedule. It is a Spring
 * service that uses the scheduling feature of Spring Framework to run the update task at regular
 * intervals.
 */
@Service
@EnableScheduling
@Profile("!test")
@Slf4j
@ConditionalOnProperty(name = "dyndrest.database.backup.enabled", havingValue = "true")
public class BackupService {

  private final JdbcTemplate jdbcTemplate;

  private boolean enabled;
  private AppConfig.Database.Backup backup;

  /**
   * The BackupService class is responsible for performing database backups based on the provided
   * configuration.
   */
  public BackupService(AppConfig appConfig, JdbcTemplate jdbcTemplate) {
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
  }

  @Transactional
  @Scheduled(cron = "${dyndrest.database.backup.cron}")
  void process() {
    if (!enabled) {
      return;
    }
    String sql = String.format("SCRIPT DROP TO '%s/dump.sql';", backup.path());
    jdbcTemplate.execute(sql);
    log.info("Database backup successful processed for path: {}", backup.path());
  }
}
