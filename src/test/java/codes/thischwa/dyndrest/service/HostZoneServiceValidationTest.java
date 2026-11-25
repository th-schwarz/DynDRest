package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.config.ZoneImportConfig;
import codes.thischwa.dyndrest.repository.HostRepo;
import codes.thischwa.dyndrest.repository.ZoneRepo;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import java.util.List;
import org.junit.jupiter.api.Test;

class HostZoneServiceValidationTest {

  @Test
  void rejectsMultiLabelHostBeforeSaving() {
    HostRepo hostRepo = mock(HostRepo.class);
    ZoneRepo zoneRepo = mock(ZoneRepo.class);
    DynamicSecurityChainManager securityChainManager = mock(DynamicSecurityChainManager.class);
    HostZoneService service = new HostZoneService(
        hostRepo, new ZoneImportConfig(null), zoneRepo, securityChainManager);
    Host host = new Host();
    host.setSld("host.subdomain");

    assertThrows(IllegalArgumentException.class, () -> service.saveOrUpdate(host));
    verifyNoInteractions(hostRepo, zoneRepo, securityChainManager);
  }

  @Test
  void rejectsMultiLabelHostImportBeforeWriting() {
    HostRepo hostRepo = mock(HostRepo.class);
    ZoneRepo zoneRepo = mock(ZoneRepo.class);
    DynamicSecurityChainManager securityChainManager = mock(DynamicSecurityChainManager.class);
    ZoneImportConfig importConfig = new ZoneImportConfig(List.of(
        new ZoneImportConfig.Zone(
            "example.com", "ns.example.com", List.of(new ZoneImportConfig.Host("host.subdomain", "token")))));
    HostZoneService service = new HostZoneService(
        hostRepo, importConfig, zoneRepo, securityChainManager);

    assertThrows(IllegalArgumentException.class, service::importOnStart);
    verifyNoInteractions(hostRepo, zoneRepo, securityChainManager);
  }
}