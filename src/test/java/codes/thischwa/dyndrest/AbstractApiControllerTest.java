package codes.thischwa.dyndrest;

import static org.mockito.Mockito.mock;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.server.ApiController;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.UpdateLogService;
import org.junit.jupiter.api.BeforeAll;

abstract class AbstractApiControllerTest {

  protected final Provider provider = mock(Provider.class);
  private final AppConfig appConfig =
      new AppConfig("dummy", false, false, 200, 4, "", false, null, null, false, null, null, "admin", "adm1n", "admin123", null);
  protected final UpdateLogService updateLogService = mock(UpdateLogService.class);
  protected final HostZoneService hostZoneService = mock(HostZoneService.class);

  protected ApiController apiController;

  @BeforeAll
  void init() {
    apiController = new ApiController(provider, appConfig, updateLogService, hostZoneService);
  }

  private int hostCount = 0;

  protected String buildHostName(String domain) {
    return String.format("host-%02d.%s", ++hostCount, domain);
  }
}
