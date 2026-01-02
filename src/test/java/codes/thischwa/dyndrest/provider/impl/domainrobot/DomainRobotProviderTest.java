package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import org.domainrobot.sdk.models.generated.Zone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class DomainRobotProviderTest extends AbstractIntegrationTest {

  private static final String zoneName = "mein-virtuelles-blech.de";
  private static final String primaryNS = "a.ns14.net";

  @Autowired
  private Provider provider;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("domainrobot.autodns.user", () -> System.getenv("DOMAINROBOT_USER"));
    registry.add("domainrobot.autodns.password", () -> System.getenv("DOMAINROBOT_PASSWORD"));
    registry.add("domainrobot.autodns.url", () -> "https://api.autodns.com");
    registry.add("domainrobot.autodns.context", () -> 4);
    registry.add("domainrobot.defaultTtl", () -> 3600);
  }

  @Test
  void testZoneInfo() throws ProviderException {
    assertInstanceOf(DomainRobotProvider.class, provider);
    DomainRobotProvider dwc = (DomainRobotProvider) provider;
    ZoneClientWrapper zcw = dwc.getZcw();

    Zone zone = zcw.info(zoneName, primaryNS);

    assertNotNull(zone);
    assertEquals(zoneName, zone.getOrigin());
    assertNotNull(zone.getResourceRecords());
    assertFalse(zone.getResourceRecords().isEmpty());
  }
}
