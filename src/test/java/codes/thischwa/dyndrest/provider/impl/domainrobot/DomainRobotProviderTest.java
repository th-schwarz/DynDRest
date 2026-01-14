package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.ResourceRecord;
import org.domainrobot.sdk.models.generated.Zone;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
public class DomainRobotProviderTest extends AbstractIntegrationTest {
  private static final String ZONE_NAME = "mein-virtuelles-blech.de";
  private static final String PRIMARY_NS = "a.ns14.net";
  private static final String API_USER = System.getenv("DOMAINROBOT_USER");
  private static final String API_PASSWORD = System.getenv("DOMAINROBOT_PASSWORD");
  private static final String SLD_PREFIX = "it-";
  private static final String DEFAULT_SLD = SLD_PREFIX + "default";
  private static final String DEFAULT_FQDN = DEFAULT_SLD + "." + ZONE_NAME;

  // Test IP constants
  private static final String TEST_IPV4_BASE = "192.0.1.1";
  private static final String TEST_IPV4_ALT = "192.0.1.11";
  private static final String TEST_IPV6_BASE = "2001:db8::10";
  private static final String TEST_IPV6_ALT = "2001:db8::11";

  @Autowired
  private Provider provider;
  @Autowired private ZoneUpdaterService zoneUpdaterService;
  private DomainRobotProvider domainRobotProvider;
  private final List<String> createdHosts = new ArrayList<>();
  private codes.thischwa.dyndrest.model.Zone DEFAULT_ZONE_MODEL;
  private Zone DEFAULT_ZONE_ADNS;
  private IpSetting DEFAULT_IP_SETTING;

  @PostConstruct
  void init() {
    zoneUpdaterService.clearForTesting();
    domainRobotProvider = (DomainRobotProvider) provider;
    try {
      DEFAULT_IP_SETTING = new IpSetting("192.0.0.1", "2001:db8::1");
      DEFAULT_ZONE_MODEL = hostZoneService.addZone(ZONE_NAME, PRIMARY_NS);
      DEFAULT_ZONE_ADNS = domainRobotProvider.getZcw().info(ZONE_NAME, PRIMARY_NS);
      cleanupDefaultHosts();
    } catch (Exception e) {
      throw new RuntimeException("Test setup failed", e);
    }
  }

  @BeforeAll
  static void checkEnv() {
    assertNotNull(API_USER, "DOMAINROBOT_USER environment variable must be set");
    assertNotNull(API_PASSWORD, "DOMAINROBOT_PASSWORD environment variable must be set");
  }

  @AfterEach
  void cleanup() {
    for (String host : createdHosts) {
      try {
        domainRobotProvider.removeHostIpSettings(host);
      } catch (Exception e) {
        // Ignore cleanup errors
      }
    }
    createdHosts.clear();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("domainrobot.autodns.user", () -> API_USER);
    registry.add("domainrobot.autodns.password", () -> API_PASSWORD);
  }

  @Test
  void testZoneInfoZcw() throws Exception {
    ZoneClientWrapper zcw = domainRobotProvider.getZcw();
    Zone zone = zcw.info(ZONE_NAME, PRIMARY_NS);
    assertNotNull(zone);
    assertEquals(ZONE_NAME, zone.getOrigin());
    assertNotNull(zone.getResourceRecords());
    assertFalse(zone.getResourceRecords().isEmpty());
  }

  @Test
  void testWorkflowHosts() throws Exception {
    testHostCreationAndAddOrUpdate();
    testZoneFetching();
    testHostRemoval();
  }

  @Test
  void testAddOrUpdateMultipleHostsWithZcw() throws Exception {
    ZoneClientWrapper zcw = domainRobotProvider.getZcw();
    Zone zone = zcw.info(ZONE_NAME, PRIMARY_NS);

    String sld1 = SLD_PREFIX + "host1";
    String sld2 = SLD_PREFIX + "host2";
    String sld3 = SLD_PREFIX + "host3";

    IpSetting ipSetting1 = new IpSetting("192.0.2.40", "2001:db8::40");
    IpSetting ipSetting2 = new IpSetting("192.0.2.41", "2001:db8::41");
    IpSetting ipSetting3 = new IpSetting("192.0.2.42", "2001:db8::42");

    processMultipleHosts(zcw, zone, List.of(sld1, sld2, sld3), List.of(ipSetting1, ipSetting2, ipSetting3));

    Zone updatedZone = zcw.info(ZONE_NAME, PRIMARY_NS);
    verifyIpSettings(zcw, updatedZone, sld1, ipSetting1);
    verifyIpSettings(zcw, updatedZone, sld2, ipSetting2);
    verifyIpSettings(zcw, updatedZone, sld3, ipSetting3);

    removeHost(sld1 + "." + ZONE_NAME);
    removeHost(sld2 + "." + ZONE_NAME);
    removeHost(sld3 + "." + ZONE_NAME);
  }

  @Test
  void testFetchZoneFromInvalidSld() {
    String invalidSld = "nonexistent-" + DEFAULT_FQDN;
    assertThrows(IllegalArgumentException.class, () -> {
      domainRobotProvider.fetchZoneFromHost(invalidSld);
    });
  }

  @Test
  void testInfoInvalidSld() {
    String invalidSld = "nonexistent-" + DEFAULT_FQDN;
    assertThrows(IllegalArgumentException.class, () -> {
      domainRobotProvider.info(invalidSld);
    });
  }

  @Test
  void testRemoveHostIpSettingsInvalidSld() {
    String invalidSld = "nonexistent-" + SLD_PREFIX + ZONE_NAME;
    assertThrows(ProviderException.class, () -> {
      domainRobotProvider.removeHostIpSettings(invalidSld);
    });
  }

  @Test
  void testPatch() throws Exception {
    // Prepare test data: 3 hosts to create
    String sldCreate1 = SLD_PREFIX + "patch-create1";
    String sldCreate2 = SLD_PREFIX + "patch-create2";
    String sldUpdate1 = SLD_PREFIX + "patch-update1";

    IpSetting ipCreate1 = new IpSetting("192.0.3.10", "2001:db8::50");
    IpSetting ipCreate2 = new IpSetting("192.0.3.11", "2001:db8::51");
    IpSetting ipUpdate1New = new IpSetting("192.0.3.20", "2001:db8::60");

    // Create HostInfoHolder instances
    HostInfoHolder create1 = new HostInfoHolder();
    create1.setZoneId(DEFAULT_ZONE_MODEL.getId());
    create1.setZone(ZONE_NAME);
    create1.setNs(PRIMARY_NS);
    create1.setSld(sldCreate1);
    create1.setIpSetting(ipCreate1);

    HostInfoHolder create2 = new HostInfoHolder();
    create2.setZoneId(DEFAULT_ZONE_MODEL.getId());
    create2.setZone(ZONE_NAME);
    create2.setNs(PRIMARY_NS);
    create2.setSld(sldCreate2);
    create2.setIpSetting(ipCreate2);

    HostInfoHolder update1 = new HostInfoHolder();
    update1.setZoneId(DEFAULT_ZONE_MODEL.getId());
    update1.setZone(ZONE_NAME);
    update1.setNs(PRIMARY_NS);
    update1.setSld(sldUpdate1);
    update1.setIpSetting(ipUpdate1New);

    // Create test lists
    List<HostInfoHolder> creates = List.of(create1, create2);
    List<HostInfoHolder> updates = List.of(update1);

    // Execute patch with creates and updates
    domainRobotProvider.patch(ZONE_NAME, creates, updates, null);

    // Verify creates
    ZoneClientWrapper zcw = domainRobotProvider.getZcw();
    Zone zone = zcw.info(ZONE_NAME, PRIMARY_NS);

    IpSetting resultCreate1 = zcw.info(zone, sldCreate1);
    assertEquals(ipCreate1, resultCreate1);

    IpSetting resultCreate2 = zcw.info(zone, sldCreate2);
    assertEquals(ipCreate2, resultCreate2);

    // Verify addOrUpdate
    IpSetting resultUpdate1 = zcw.info(zone, sldUpdate1);
    assertEquals(ipUpdate1New, resultUpdate1);

    // Test delete operation
    List<String> deletes = List.of(sldCreate1, sldCreate2);
    domainRobotProvider.patch(ZONE_NAME, null, null, deletes);

    // Verify deletes
    zone = zcw.info(ZONE_NAME, PRIMARY_NS);
    IpSetting resultAfterDelete1 = zcw.info(zone, sldCreate1);
    assertTrue(resultAfterDelete1.isNotSet());

    IpSetting resultAfterDelete2 = zcw.info(zone, sldCreate2);
    assertTrue(resultAfterDelete2.isNotSet());

    // Cleanup
    removeHost(sldUpdate1 + "." + ZONE_NAME);
  }

  private void cleanupDefaultHosts() {
    ZoneClientWrapper zcw = domainRobotProvider.getZcw();
    List<ResourceRecord> resourceRecords = DEFAULT_ZONE_ADNS.getResourceRecords();
    
    // Collect records to remove
    List<ResourceRecord> recordsToRemove = new ArrayList<>();
    for (ResourceRecord resourceRecord : resourceRecords) {
      if (resourceRecord.getName().startsWith(SLD_PREFIX)) {
        recordsToRemove.add(resourceRecord);
      }
    }
    
    // Remove the collected records
    for (ResourceRecord recordToRemove : recordsToRemove) {
      zcw.removeIp(DEFAULT_ZONE_ADNS, recordToRemove.getName(), 
          ZoneClientWrapper.ResourceRecordTypeIp.valueOf(recordToRemove.getType()));
    }
    
    try {
      zcw.update(DEFAULT_ZONE_ADNS);
    } catch (ProviderException e) {
      log.warn("Error while updating zone {}", DEFAULT_ZONE_ADNS.getOrigin(), e);
    }
  }

  private void testHostCreationAndAddOrUpdate() throws Exception {
    Host host1 = activateHost(SLD_PREFIX + "host1", DEFAULT_IP_SETTING);
    verifyHostIpSetting(host1, DEFAULT_IP_SETTING);

    IpSetting ipv4OnlyUpdate = new IpSetting(TEST_IPV4_BASE, DEFAULT_IP_SETTING.getIpv6().getHostAddress());
    Host host2 = activateHost(SLD_PREFIX + "host2", ipv4OnlyUpdate);
    verifyHostIpSetting(host2, ipv4OnlyUpdate);

    IpSetting ipv6OnlyUpdate = new IpSetting(TEST_IPV4_BASE, TEST_IPV6_BASE);
    Host host3 = activateHost(SLD_PREFIX + "host3", ipv6OnlyUpdate);
    verifyHostIpSetting(host3, ipv6OnlyUpdate);

    IpSetting bothIpsUpdate = new IpSetting(TEST_IPV4_ALT, TEST_IPV6_ALT);
    Host host4 = activateHost(SLD_PREFIX + "host4", bothIpsUpdate);
    verifyHostIpSetting(host4, bothIpsUpdate);
  }

  private void testZoneFetching() throws Exception {
    Host host = activateHost(SLD_PREFIX + "host1", DEFAULT_IP_SETTING);
    Zone zone = domainRobotProvider.fetchZoneFromHost(host.getSld() + "." + ZONE_NAME);
    assertNotNull(zone);
    assertEquals(ZONE_NAME, zone.getOrigin());
  }

  private void testHostRemoval() throws Exception {
    List<Host> hosts = List.of(
        activateHost(SLD_PREFIX + "host1", DEFAULT_IP_SETTING),
        activateHost(SLD_PREFIX + "host2", DEFAULT_IP_SETTING),
        activateHost(SLD_PREFIX + "host3", DEFAULT_IP_SETTING),
        activateHost(SLD_PREFIX + "host4", DEFAULT_IP_SETTING)
    );

    for (Host host : hosts) {
      verifyHostRemoval(host);
    }
  }

  private void verifyHostIpSetting(Host host, IpSetting expected) throws ProviderException {
    IpSetting actual = domainRobotProvider.info(host.getSld() + "." + ZONE_NAME);
    assertEquals(expected, actual);
  }

  private void verifyHostRemoval(Host host) throws ProviderException {
    String fqdn = host.getSld() + "." + ZONE_NAME;
    domainRobotProvider.removeHostIpSettings(fqdn);
    IpSetting removedSetting = domainRobotProvider.info(fqdn);
    assertTrue(removedSetting.isNotSet());
    removeHost(host);
  }

  private void processMultipleHosts(ZoneClientWrapper zcw, Zone zone, List<String> slds, List<IpSetting> ipSettings)
      throws ProviderException {
    for (int i = 0; i < slds.size(); i++) {
      zcw.process(zone, slds.get(i), ipSettings.get(i));
      createdHosts.add(slds.get(i));
    }
    zcw.update(zone);
  }

  private void verifyIpSettings(ZoneClientWrapper zcw, Zone zone, String sld, IpSetting expected) {
    IpSetting result = zcw.info(zone, sld);
    assertNotNull(result);
    assertEquals(expected.getIpv4(), result.getIpv4());
    assertEquals(expected.getIpv6(), result.getIpv6());
  }

  private Host activateHost(String sld, @Nullable IpSetting ipSetting) {
    try {
      Host host;
      if (!hostZoneService.hostExists(sld + "." + ZONE_NAME)) {
        host = hostZoneService.addHost(DEFAULT_ZONE_MODEL, sld, "1234567890abcdef");
      } else {
        host = hostZoneService.getHost(sld + "." + ZONE_NAME).get();
      }
      if (ipSetting != null) {
        domainRobotProvider.addOrUpdate(sld + "." + ZONE_NAME, ipSetting);
      }
      return host;
    } catch (ProviderException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeHost(Host host) {
    hostZoneService.deleteHost(host);
    try {
      domainRobotProvider.removeHostIpSettings(host.getSld() + "." + ZONE_NAME);
    } catch (ProviderException e) {
      log.warn("Error while removing host {}", host.getSld(), e);
    }
  }

  private void removeHost(String fqdn) {
    Optional<HostEnriched> hostEnrichedOpt = hostZoneService.getHost(fqdn);
    hostEnrichedOpt.ifPresent(this::removeHost);
  }
}
