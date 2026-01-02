package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import jakarta.annotation.PostConstruct;
import org.domainrobot.sdk.models.generated.Zone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class DomainRobotProviderTest extends AbstractIntegrationTest {

  private static final String zoneName = "mein-virtuelles-blech.de";
  private static final String primaryNS = "a.ns14.net";

  private static final String DOMAINROBOT_USER = System.getenv("DOMAINROBOT_USER");
  private static final String DOMAINROBOT_PASSWORD = System.getenv("DOMAINROBOT_PASSWORD");

  @Autowired
  private Provider provider;

  private DomainRobotProvider domainRobotProvider;

  @PostConstruct
  void init() {
    domainRobotProvider = (DomainRobotProvider) provider;
  }

  @BeforeAll
  static void checkEnv() {
    assertNotNull(DOMAINROBOT_USER, "DOMAINROBOT_USER environment variable must be set");
    assertNotNull(DOMAINROBOT_PASSWORD, "DOMAINROBOT_PASSWORD environment variable must be set");
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("domainrobot.autodns.user", () -> System.getenv("DOMAINROBOT_USER"));
    registry.add("domainrobot.autodns.password", () -> System.getenv("DOMAINROBOT_PASSWORD"));
  }

  @Test
  void testZoneInfo() throws ProviderException {
    ZoneClientWrapper zcw = domainRobotProvider.getZcw();
    Zone zone = zcw.info(zoneName, primaryNS);

    assertNotNull(zone);
    assertEquals(zoneName, zone.getOrigin());
    assertNotNull(zone.getResourceRecords());
    assertFalse(zone.getResourceRecords().isEmpty());
  }
}
