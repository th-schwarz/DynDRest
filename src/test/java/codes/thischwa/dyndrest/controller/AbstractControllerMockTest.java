package codes.thischwa.dyndrest.controller;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.server.controller.ApiController;
import codes.thischwa.dyndrest.server.controller.RouterController;
import codes.thischwa.dyndrest.service.ControllerService;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.UpdateLogService;
import codes.thischwa.dyndrest.service.ZoneUpdaterScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractControllerMockTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public AppConfig appConfig() {
      return new AppConfig("dummy", false, false, 200, 4, "", false, null, null, false, null, null, "admin", "adm1n", false, 0);
    }
  }

  @MockitoBean
  protected Provider provider;

  @MockitoBean
  protected UpdateLogService updateLogService;

  @MockitoBean
  protected HostZoneService hostZoneService;

  @MockitoBean
  protected DynamicSecurityChainManager dynamicSecurityChainManager;

  @Autowired
  protected ControllerService controllerService;

  @MockitoBean
  protected ZoneUpdaterScheduler zoneUpdaterScheduler;

  @MockitoBean
  protected ZoneUpdaterService zoneUpdaterService;

  @Autowired
  protected ApiController apiController;

  @Autowired
  protected RouterController routerController;

  @Autowired
  protected AppConfig appConfig;

  private int hostCount = 0;

  protected String buildHostName(String domain) {
    return String.format("host-%02d.%s", ++hostCount, domain);
  }
}
