package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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
  private static final String DOMAINROBOT_USER = System.getenv("DOMAINROBOT_USER");
  private static final String DOMAINROBOT_PASSWORD = System.getenv("DOMAINROBOT_PASSWORD");
  private static final String SLD_PREFIX = "it-";
  private static String DEFAULT_SLD = SLD_PREFIX + "default";
  private static final String DEFAULT_FQDN = DEFAULT_SLD + "." + ZONE_NAME;
  private static final List<String> SLDS_TO_USE = List.of("host1", "host2", "host3", "host4");

  // Test IP constants
  private static final String TEST_IPV4_BASE = "192.0.1.1";
  private static final String TEST_IPV4_ALT = "192.0.1.11";
  private static final String TEST_IPV6_BASE = "2001:db8::10";
  private static final String TEST_IPV6_ALT = "2001:db8::11";

  @Autowired
  private Provider provider;
  private DomainRobotProvider domainRobotProvider;
  private final List<String> createdHosts = new ArrayList<>();
  private codes.thischwa.dyndrest.model.Zone DEFAULT_ZONE_MODEL;
  private Zone DEFAULT_ZONE_ADNS;
  private IpSetting DEFAULT_IP_SETTING;

  @PostConstruct
  void init() {
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
    assertNotNull(DOMAINROBOT_USER, "DOMAINROBOT_USER environment variable must be set");
    assertNotNull(DOMAINROBOT_PASSWORD, "DOMAINROBOT_PASSWORD environment variable must be set");
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
    registry.add("domainrobot.autodns.user", () -> System.getenv("DOMAINROBOT_USER"));
    registry.add("domainrobot.autodns.password", () -> System.getenv("DOMAINROBOT_PASSWORD"));
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
    testHostCreationAndUpdate();
    testZoneFetching();
    testHostRemoval();
  }

  @Test
  void testUpdateMultipleHostsWithZcw() throws Exception {
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

  private void cleanupDefaultHosts() {
    for (String sld : SLDS_TO_USE) {
      removeHost(sld + "." + ZONE_NAME);
    }
  }

  private void testHostCreationAndUpdate() throws Exception {
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
        domainRobotProvider.update(sld + "." + ZONE_NAME, ipSetting);
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
      // Ignore
    }
  }

  private void removeHost(String fqdn) {
    Optional<HostEnriched> hostEnrichedOpt = hostZoneService.getHost(fqdn);
    hostEnrichedOpt.ifPresent(this::removeHost);
  }
}
