package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
public class DomainRobotProviderTest extends AbstractIntegrationTest {

  private static final String zoneName = "mein-virtuelles-blech.de";
  private static final String primaryNS = "a.ns14.net";

  private static final String DOMAINROBOT_USER = System.getenv("DOMAINROBOT_USER");
  private static final String DOMAINROBOT_PASSWORD = System.getenv("DOMAINROBOT_PASSWORD");

  private static final String SLD_PREFIX = "it-";

  private static final String DEFAULT_FQDN = SLD_PREFIX + "default." + zoneName;

  private static final String TEST_LIVE_HOST = "test." + zoneName;

  @Autowired
  private Provider provider;

  private DomainRobotProvider domainRobotProvider;

  private final List<String> createdHosts = new ArrayList<>();

  @PostConstruct
  void init() {
    domainRobotProvider = (DomainRobotProvider) provider;

    try {
      // add live host
      codes.thischwa.dyndrest.model.Zone liveZone = hostZoneService.addZone("mein-virtuelles-blech.de", "a.ns14.net");
      hostZoneService.addHost(liveZone, "test", "1234567890abcdef");
      IpSetting ipSetting = new IpSetting("127.0.0.1", "2001:db8::1");
      domainRobotProvider.update(TEST_LIVE_HOST, ipSetting);
    } catch (Exception e) {
      log.warn("Error setting up test environment: " + e.getMessage(), e);
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
  void testZoneInfo() throws Exception {
    ZoneClientWrapper zcw = domainRobotProvider.getZcw();
    Zone zone = zcw.info(zoneName, primaryNS);

    assertNotNull(zone);
    assertEquals(zoneName, zone.getOrigin());
    assertNotNull(zone.getResourceRecords());
    assertFalse(zone.getResourceRecords().isEmpty());
  }

  @Test
  void testUpdateAndInfo() throws Exception {
    IpSetting ipSetting = new IpSetting("192.0.2.1", "2001:db8::1");

    domainRobotProvider.update(TEST_LIVE_HOST, ipSetting);
    createdHosts.add(TEST_LIVE_HOST);

    IpSetting result = domainRobotProvider.info(TEST_LIVE_HOST);
    assertNotNull(result);
    assertEquals(ipSetting.getIpv4(), result.getIpv4());
    assertEquals(ipSetting.getIpv6(), result.getIpv6());
  }

  @Test
  void testUpdateWithoutIpChange() throws Exception {
    IpSetting ipSetting = new IpSetting("192.0.2.2", "2001:db8::2");

    domainRobotProvider.update(TEST_LIVE_HOST, ipSetting);
    domainRobotProvider.update(TEST_LIVE_HOST, ipSetting);

    IpSetting result = domainRobotProvider.info(TEST_LIVE_HOST);
    assertNotNull(result);
    assertEquals(ipSetting.getIpv4(), result.getIpv4());
    assertEquals(ipSetting.getIpv6(), result.getIpv6());
  }

  @Test
  void testUpdateOnlyIpv4() throws Exception {
    IpSetting ipSetting = new IpSetting("192.0.2.10", null);

    domainRobotProvider.update(TEST_LIVE_HOST, ipSetting);

    IpSetting result = domainRobotProvider.info(TEST_LIVE_HOST);
    assertNotNull(result);
    assertEquals(ipSetting.getIpv4(), result.getIpv4());
    assertNull(result.getIpv6());
  }

  @Test
  void testUpdateOnlyIpv6() throws Exception {
    IpSetting ipSetting = new IpSetting(null, "2001:db8::10");

    domainRobotProvider.update(TEST_LIVE_HOST, ipSetting);

    IpSetting result = domainRobotProvider.info(TEST_LIVE_HOST);
    assertNotNull(result);
    assertNull(result.getIpv4());
    assertEquals(ipSetting.getIpv6(), result.getIpv6());
  }

  @Test
  void testUpdateChangeIps() throws Exception {
    IpSetting ipSetting1 = new IpSetting("192.0.2.20", "2001:db8::20");
    IpSetting ipSetting2 = new IpSetting("192.0.2.21", "2001:db8::21");

    domainRobotProvider.update(TEST_LIVE_HOST, ipSetting1);
    createdHosts.add(TEST_LIVE_HOST);
    domainRobotProvider.update(TEST_LIVE_HOST, ipSetting2);

    IpSetting result = domainRobotProvider.info(TEST_LIVE_HOST);
    assertNotNull(result);
    assertEquals(ipSetting2.getIpv4(), result.getIpv4());
    assertEquals(ipSetting2.getIpv6(), result.getIpv6());
  }

  @Test
  void testFetchZoneFromHost() throws Exception {
    Zone zone = domainRobotProvider.fetchZoneFromHost(TEST_LIVE_HOST);

    assertNotNull(zone);
    assertEquals(zoneName, zone.getOrigin());
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
  void testRemoveHostIpSettingsinvalidSld() {
    String invalidSld = "nonexistent-" + SLD_PREFIX + zoneName;

    assertThrows(ProviderException.class, () -> {
      domainRobotProvider.removeHostIpSettings(invalidSld);
    });
  }

  @Test
  void testUpdateMultipleHostsAndDelete() throws Exception {
    String sld1 = SLD_PREFIX + "host1";
    String sld2 = SLD_PREFIX + "host2";
    String sld3 = SLD_PREFIX + "host3";

    codes.thischwa.dyndrest.model.Zone zone = hostZoneService.getZone(zoneName);
    Host host1 = hostZoneService.addHost(zone, sld1, "1234567890abcdef");
    Host host2 = hostZoneService.addHost(zone, sld2, "1234567890abcdef");
    Host host3 = hostZoneService.addHost(zone, sld3, "1234567890abcdef");

    IpSetting ipSetting1 = new IpSetting("192.0.2.30", "2001:db8::30");
    IpSetting ipSetting2 = new IpSetting("192.0.2.31", "2001:db8::31");
    IpSetting ipSetting3 = new IpSetting("192.0.2.32", "2001:db8::32");

    String fqdn1 = sld1 + "." + zoneName;
    String fqdn2 = sld2 + "." + zoneName;
    String fqdn3 = sld3 + "." + zoneName;
    domainRobotProvider.update(fqdn1, ipSetting1);
    createdHosts.add(fqdn1);
    domainRobotProvider.update(fqdn2, ipSetting2);
    createdHosts.add(fqdn2);
    domainRobotProvider.update(fqdn3, ipSetting3);
    createdHosts.add(fqdn3);

    IpSetting result1 = domainRobotProvider.info(fqdn1);
    IpSetting result2 = domainRobotProvider.info(fqdn2);
    IpSetting result3 = domainRobotProvider.info(fqdn3);

    assertNotNull(result1);
    assertEquals(ipSetting1.getIpv4(), result1.getIpv4());
    assertEquals(ipSetting1.getIpv6(), result1.getIpv6());

    assertNotNull(result2);
    assertEquals(ipSetting2.getIpv4(), result2.getIpv4());
    assertEquals(ipSetting2.getIpv6(), result2.getIpv6());

    assertNotNull(result3);
    assertEquals(ipSetting3.getIpv4(), result3.getIpv4());
    assertEquals(ipSetting3.getIpv6(), result3.getIpv6());

    domainRobotProvider.removeHostIpSettings(fqdn1);
    codes.thischwa.dyndrest.model.IpSetting ipSetting = domainRobotProvider.info(fqdn1);
    assertTrue(ipSetting.isNotSet());
    createdHosts.remove(fqdn1);
  }

  @Test
  void testUpdateMultipleHostsWithZcw() throws Exception {
    ZoneClientWrapper zcw = domainRobotProvider.getZcw();
    Zone zone = zcw.info(zoneName, primaryNS);

    String sld1 = SLD_PREFIX + "host1";
    String sld2 = SLD_PREFIX + "host2";
    String sld3 = SLD_PREFIX + "host3";

    IpSetting ipSetting1 = new IpSetting("192.0.2.40", "2001:db8::40");
    IpSetting ipSetting2 = new IpSetting("192.0.2.41", "2001:db8::41");
    IpSetting ipSetting3 = new IpSetting("192.0.2.42", "2001:db8::42");

    zcw.process(zone, sld1, ipSetting1);
    createdHosts.add(sld1);
    zcw.process(zone, sld2, ipSetting2);
    createdHosts.add(sld2);
    zcw.process(zone, sld3, ipSetting3);
    createdHosts.add(sld3);
    zcw.update(zone);

    Zone updatedZone = zcw.info(zoneName, primaryNS);
    IpSetting result1 = zcw.info(updatedZone, sld1);
    IpSetting result2 = zcw.info(updatedZone, sld2);
    IpSetting result3 = zcw.info(updatedZone, sld3);

    assertNotNull(result1);
    assertEquals(ipSetting1.getIpv4(), result1.getIpv4());
    assertEquals(ipSetting1.getIpv6(), result1.getIpv6());

    assertNotNull(result2);
    assertEquals(ipSetting2.getIpv4(), result2.getIpv4());
    assertEquals(ipSetting2.getIpv6(), result2.getIpv6());

    assertNotNull(result3);
    assertEquals(ipSetting3.getIpv4(), result3.getIpv4());
    assertEquals(ipSetting3.getIpv6(), result3.getIpv6());
  }
}
