package codes.thischwa.dyndrest.provider.impl.cloudflare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.cf.model.ZoneEntity;
import codes.thischwa.dyndrest.AbstractCloudflareTest;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.service.HostZoneService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
public class CloudflareProviderTest extends AbstractCloudflareTest {
  private static final String ZONE_NAME = "mein-d-ns.de";
  private static final String API_EMAIL = System.getenv("CF_API_EMAIL");
  private static final String API_KEY = System.getenv("CF_API_KEY");
  private static final String SLD_PREFIX = "it-";
  private static final String DEFAULT_SLD = SLD_PREFIX + "default";
  private static final String DEFAULT_FQDN = DEFAULT_SLD + "." + ZONE_NAME;
  private static final List<String> SLDS_TO_USE = List.of("host1", "host2", "host3", "host4");

  // Test IP constants
  private static final String TEST_IPV4_BASE = "192.0.1.1";
  private static final String TEST_IPV4_ALT = "192.0.1.11";
  private static final String TEST_IPV6_BASE = "2001:db8::10";
  private static final String TEST_IPV6_ALT = "2001:db8::11";

  @Autowired
  private Provider provider;
  @Autowired
  private HostZoneService hostZoneService;
  private CloudflareProvider cloudflareProvider;
  private codes.thischwa.dyndrest.model.Zone DEFAULT_ZONE_MODEL;
  private ZoneEntity DEFAULT_ZONE_CF;
  private IpSetting DEFAULT_IP_SETTING;

  @PostConstruct
  void init() {
    cloudflareProvider = (CloudflareProvider) provider;
    try {
      DEFAULT_IP_SETTING = new IpSetting("192.0.0.1", "2001:db8::1");
      DEFAULT_ZONE_MODEL = hostZoneService.getZone(ZONE_NAME);
      DEFAULT_ZONE_CF = cloudflareProvider.cfDnsClient.zoneGet(ZONE_NAME);
      cleanupDefaultHosts();
    } catch (Exception e) {
      throw new RuntimeException("Test setup failed", e);
    }
  }

  @BeforeAll
  static void checkEnv() {
    assertNotNull(API_EMAIL, "CF_API_EMAIL environment variable must be set");
    assertNotNull(API_KEY, "CF_API_KEY environment variable must be set");
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("cloudflare.email", () -> API_EMAIL);
    registry.add("cloudflare.api-key", () -> API_KEY);
  }

  @Test
  void testZoneInfo() throws Exception {
    ZoneEntity zone = cloudflareProvider.cfDnsClient.zoneGet(ZONE_NAME);
    assertNotNull(zone);
    assertEquals(ZONE_NAME, zone.getName());
    assertNotNull(zone.getId());
  }

  @Test
  void testWorkflowHosts() throws Exception {
    testHostCreationAndUpdate();
    testZoneFetching();
    testHostRemoval();
  }

  @Test
  void testFetchZoneFromInvalidSld() {
    String invalidSld = "nonexistent-" + DEFAULT_FQDN;
    assertThrows(IllegalArgumentException.class, () -> {
      cloudflareProvider.fetchZoneFromHost(invalidSld);
    });
  }

  @Test
  void testInfoInvalidSld() {
    String invalidSld = "nonexistent-" + DEFAULT_FQDN;
    assertThrows(IllegalArgumentException.class, () -> {
      cloudflareProvider.info(invalidSld);
    });
  }

  @Test
  void testRemoveHostIpSettingsInvalidSld() {
    String invalidSld = "nonexistent-" + SLD_PREFIX + ZONE_NAME;
    assertThrows(ProviderException.class, () -> {
      cloudflareProvider.removeHostIpSettings(invalidSld);
    });
  }

  private void cleanupDefaultHosts() {
    for (String sld : SLDS_TO_USE) {
      removeHost(SLD_PREFIX + sld + "." + ZONE_NAME);
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
    ZoneEntity zone = cloudflareProvider.fetchZoneFromHost(host.getSld() + "." + ZONE_NAME);
    assertNotNull(zone);
    assertEquals(ZONE_NAME, zone.getName());
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
    IpSetting actual = cloudflareProvider.info(host.getSld() + "." + ZONE_NAME);
    assertEquals(expected, actual);
  }

  private void verifyHostRemoval(Host host) throws ProviderException {
    String fqdn = host.getSld() + "." + ZONE_NAME;
    cloudflareProvider.removeHostIpSettings(fqdn);
    IpSetting removedSetting = cloudflareProvider.info(fqdn);
    assertTrue(removedSetting.isNotSet());
    removeHost(host);
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
        cloudflareProvider.update(sld + "." + ZONE_NAME, ipSetting);
      }
      return host;
    } catch (ProviderException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeHost(Host host) {
    hostZoneService.deleteHost(host);
    try {
      cloudflareProvider.removeHostIpSettings(host.getSld() + "." + ZONE_NAME);
    } catch (ProviderException e) {
      // Ignore
    }
  }

  private void removeHost(String fqdn) {
    Optional<HostEnriched> hostEnrichedOpt = hostZoneService.getHost(fqdn);
    hostEnrichedOpt.ifPresent(this::removeHost);
  }
}
