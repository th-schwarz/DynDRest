package codes.thischwa.dyndrest.controller;

import static org.mockito.Mockito.mock;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.server.controller.ApiController;
import codes.thischwa.dyndrest.service.ControllerService;
import codes.thischwa.dyndrest.server.controller.RouterController;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.UpdateLogService;
import org.junit.jupiter.api.BeforeAll;

abstract class AbstractControllerTest {

  protected final Provider provider = mock(Provider.class);
  private final AppConfig appConfig =
      new AppConfig("dummy", false, false, 200, 4, "", false, null, null, false, null, null, "admin", "adm1n");
  protected final UpdateLogService updateLogService = mock(UpdateLogService.class);
  protected final HostZoneService hostZoneService = mock(HostZoneService.class);
  protected final DynamicSecurityChainManager dynamicSecurityChainManager = mock(DynamicSecurityChainManager.class);
  protected final ControllerService controllerService = mock(ControllerService.class);

  protected ApiController apiController;
  protected RouterController routerController;

  @BeforeAll
  void init() {
    apiController = new ApiController(provider, hostZoneService, controllerService);
    routerController = new RouterController(controllerService, hostZoneService, dynamicSecurityChainManager);
  }

  private int hostCount = 0;

  protected String buildHostName(String domain) {
    return String.format("host-%02d.%s", ++hostCount, domain);
  }
}
