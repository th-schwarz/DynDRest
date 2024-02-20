package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.service.HostZoneService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
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

  /**
   * The ApplicationStartup constructor initializes the ApplicationStartup object.
   *
   * @param config The base configuration of the application.
   * @param env The environment settings.
   * @param hostZoneService The HostZoneService.
   */
  public ApplicationStartup(AppConfig config, Environment env, HostZoneService hostZoneService) {
    this.config = config;
    this.env = env;
    this.hostZoneService = hostZoneService;
  }

  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {
    String profiles = String.join(",", env.getActiveProfiles());
    log.info("*** Settings for DynDRest:");
    log.info("  * active profile(s): {}", StringUtils.isEmpty(profiles) ? "n/a" : profiles);
    log.info("  * provider: {}", config.provider());
    log.info("  * greeting-enabled: {}", config.greetingEnabled());
    log.info("  * host-validation-enabled: {}", config.hostValidationEnabled());
    log.info("  * update-log-page-enabled: {}", config.updateLogPageEnabled());
    log.info("");
    log.info("*** Endpoints:");

    ApplicationContext applicationContext = event.getApplicationContext();
    RequestMappingHandlerMapping requestMappingHandlerMapping =
        applicationContext.getBean(
            "requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
    Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
    map.forEach((key, value) -> log.info("  * {}", key));

    hostZoneService.importOnStart();
  }
}
