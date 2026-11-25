package codes.thischwa.dyndrest.server.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.server.controller.ApiController;
import codes.thischwa.dyndrest.service.ControllerService;
import codes.thischwa.dyndrest.service.HostZoneService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

class DynamicSecurityChainManagerLoggingTest {

  private final Logger apiLogger = (Logger) LoggerFactory.getLogger(
      "codes.thischwa.dyndrest.server.controller.ApiController");
  private final Logger securityLogger =
      (Logger) LoggerFactory.getLogger(
          "codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager");
  private final Level originalApiLevel = apiLogger.getLevel();
  private final Level originalSecurityLevel = securityLogger.getLevel();
  private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

  @AfterEach
  void cleanUp() {
    apiLogger.detachAppender(appender);
    securityLogger.detachAppender(appender);
    apiLogger.setLevel(originalApiLevel);
    securityLogger.setLevel(originalSecurityLevel);
    appender.stop();
  }

  @Test
  void apiTokenIsNotIncludedInDebugLogs() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    appender.setContext(loggerContext);
    appender.start();
    apiLogger.setLevel(Level.DEBUG);
    securityLogger.setLevel(Level.DEBUG);
    apiLogger.addAppender(appender);
    securityLogger.addAppender(appender);

    String apiToken = "sensitive-token-123";
    HostZoneService hostZoneService = mock(HostZoneService.class);
    ControllerService controllerService = mock(ControllerService.class);
    when(hostZoneService.validate("host.example.com", apiToken)).thenReturn(true);
    when(controllerService.processIpUpdate(
        eq("host.example.com"),
        isNull(InetAddress.class),
        isNull(InetAddress.class),
        any(HttpServletRequest.class)))
        .thenReturn(ResponseEntity.ok().build());
    ApiController apiController = new ApiController(mock(Provider.class), hostZoneService, controllerService);
    apiController.updateHost("host.example.com", apiToken, null, null, new MockHttpServletRequest());

    HostEnriched host = new HostEnriched();
    host.setSld("host");
    host.setZone("example.com");
    host.setApiToken(apiToken);
    new DynamicSecurityChainManager(mock(InMemoryUserDetailsManager.class)).addOrUpdateHost(host);

    String loggedMessages = appender.list.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.joining("\n"));
    assertFalse(loggedMessages.contains(apiToken));
    assertFalse(loggedMessages.contains(apiToken.substring(0, 4)));
  }
}