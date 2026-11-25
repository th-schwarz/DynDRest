package codes.thischwa.dyndrest.provider.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.model.config.ZoneImportConfig;
import codes.thischwa.dyndrest.repository.HostRepo;
import codes.thischwa.dyndrest.repository.ZoneRepo;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

class GenericProviderHostValidationTest {

  @Test
  void rejectsExistingMultiLabelHostBeforeCallingProvider() {
    HostEnriched invalidHost = new HostEnriched();
    invalidHost.setSld("old.host");
    HostRepo hostRepo = mock(HostRepo.class);
    when(hostRepo.findAllExtended()).thenReturn(List.of(invalidHost));
    HostZoneService hostZoneService = new HostZoneService(
        hostRepo,
        new ZoneImportConfig(null),
        mock(ZoneRepo.class),
        mock(DynamicSecurityChainManager.class));
    TestProvider provider = new TestProvider(
        new AppConfig(
            "dummy", false, false, 4, "", null, null, null, null, "admin", "adm1n", false, 0),
        new DynamicSecurityChainManager(mock(InMemoryUserDetailsManager.class)),
        hostZoneService,
        mock(ZoneUpdaterService.class));

    assertThrows(IllegalArgumentException.class, provider::validateHostZoneConfiguration);
    assertFalse(provider.providerAccessed);
  }

  private static class TestProvider extends GenericProvider {

    private boolean providerAccessed;

    TestProvider(
        AppConfig appConfig,
        DynamicSecurityChainManager securityChainManager,
        HostZoneService hostZoneService,
        ZoneUpdaterService zoneUpdaterService) {
      super(appConfig, securityChainManager, hostZoneService, zoneUpdaterService);
    }

    @Override
    public void confirmZone(Zone zone) {}

    @Override
    public List<HostInfoHolder> getCurrentConfiguredHosts(List<HostEnriched> hostsEnriched) {
      providerAccessed = true;
      return List.of();
    }

    @Override
    public void addOrUpdate(String fqdn, IpSetting ipSetting) {}

    @Override
    public void removeHostIpSettings(String fqdn) {}

    @Override
    public void patch(
        String zone,
        @Nullable List<HostInfoHolder> creates,
        @Nullable List<HostInfoHolder> updates,
        @Nullable List<String> deletes) {}
  }
}