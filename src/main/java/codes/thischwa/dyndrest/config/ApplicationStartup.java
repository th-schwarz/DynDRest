package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.repository.UpdateLogRepo;
import codes.thischwa.dyndrest.service.HostZoneService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** The ApplicationStartup class processes some tasks if the application is ready to start. */
@Component
@Profile("!test")
@Slf4j
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

  private final AppConfig config;

  private final Environment env;

  private final HostZoneService hostZoneService;
  private final UpdateLogRepo updateLogRepo;

  /**
   * Initializes the application on startup.
   *
   * @param config the application configuration
   * @param env the application environment
   * @param hostZoneService the host zone service
   * @param updateLogRepo the update log repository
   */
  public ApplicationStartup(
      AppConfig config,
      Environment env,
      HostZoneService hostZoneService,
      UpdateLogRepo updateLogRepo) {
    this.config = config;
    this.env = env;
    this.hostZoneService = hostZoneService;
    this.updateLogRepo = updateLogRepo;
  }

  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {
    String profiles = String.join(",", env.getActiveProfiles());
    log.info("*** Settings for DynDRest:");
    log.info("  * active profile(s): {}", !StringUtils.hasText(profiles) ? "n/a" : profiles);
    log.info("  * provider: {}", config.provider());
    log.info("  * greeting-enabled: {}", config.greetingEnabled());
    log.info("  * host-validation-enabled: {}", config.hostValidationEnabled());
    log.info("  * update-log-page-enabled: {}", config.updateLogPageEnabled());
    log.info("h2 setting:");
    log.info("  - spring.h2.console.enabled: {}", env.getProperty("spring.h2.console.enabled"));
    log.info("  - spring.h2.console.path: {}", env.getProperty("spring.h2.console.path"));
    log.info("  - dyndrest.database.dump-file: {}", env.getProperty("dyndrest.database.dump-file"));
    log.info("  - backup enabled: {}",
        config.database().backup() != null && config.database().backup().enabled());
    log.info(
        "  - restore enabled: {}",
        config.database().restore() != null && config.database().restore().enabled());
    log.info("*** Endpoints:");

    ApplicationContext applicationContext = event.getApplicationContext();
    RequestMappingHandlerMapping requestMappingHandlerMapping =
        applicationContext.getBean(
            "requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
    Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
    map.forEach((key, value) -> log.info("  * {}", key));
    hostZoneService.importOnStart();
    // buildDummyUpdateLogs();
  }

  private void buildDummyUpdateLogs() {
    try {
      LocalDateTime dateTime = LocalDateTime.now();
      updateLogRepo.save(
          UpdateLog.getInstance(
              1,
              new IpSetting("198.0.1.0", "2a03:4000:41:32:0:0:1:0"),
              UpdateLog.Status.success,
              dateTime,
              dateTime));
      for (int i = 1; i <= 150; i++) {
        dateTime = dateTime.plusMinutes(10);
        updateLogRepo.save(
            UpdateLog.getInstance(
                1, new IpSetting("198.0.1." + i), UpdateLog.Status.success, dateTime, dateTime));

        log.info("Dummy logs created");
      }
    } catch (Exception e) {
      log.error("Error creating dummy logs", e);
    }
  }
}
