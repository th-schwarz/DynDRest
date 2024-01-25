package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.config.ZoneConfig;
import codes.thischwa.dyndrest.model.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZoneConfigAndServiceTest extends GenericIntegrationTest {

  private final int configuredEntries = 2;

  @Autowired private ZoneService zoneService;

  @Autowired private ZoneConfig zoneConfig;

  @BeforeEach
  void setUp() {
    zoneService.read();
  }

  @Test
  final void testGetApiToken() {
    assertEquals("1234567890abcdef", zoneService.getApitoken("my0.dynhost0.info"));
    assertThrows(
        IllegalArgumentException.class, () -> zoneService.getApitoken("unknown.host.info"));
  }

  @Test
  final void testWrongHostFormat() {
    String wrongHost = "wrong-formatted.host";
    Zone z = zoneConfig.zones().get(0);
    z.hosts().add(wrongHost);
    assertThrows(IllegalArgumentException.class, zoneService::read);
    z.hosts().remove(wrongHost);
  }

  @Test
  final void testEmptyHosts() {
    Zone z = zoneConfig.zones().get(1);
    List<String> hosts = new ArrayList<>(z.hosts());
    z.hosts().clear();
    assertThrows(IllegalArgumentException.class, zoneService::read);
    z.hosts().addAll(hosts);
  }

  @Test
  final void testGetPrimaryNameServer() {
    assertEquals("ns1.domain.info", zoneService.getPrimaryNameServer("dynhost1.info"));
    assertThrows(
        IllegalArgumentException.class,
        () -> zoneService.getPrimaryNameServer("unknown-host.info"));
  }

  @Test
  final void testConfigured() {
    assertEquals(configuredEntries * 2, zoneService.getConfiguredHosts().size());
    assertEquals(configuredEntries, zoneService.getConfiguredZones().size());
  }

  @Test
  final void testValidateData_ok() {
    zoneService.validate();
  }
}
