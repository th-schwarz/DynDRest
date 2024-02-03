package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.config.ZoneConfig;
import codes.thischwa.dyndrest.model.Zone;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DomainRobotConfigValidationTest extends AbstractIntegrationTest {

  private static Validator validator;
  private static ValidatorFactory validatorFactory;
  private final int configuredEntries = 2;
  @Autowired private AppConfig appConfig;

  @Autowired private ZoneConfig zoneConfig;

  @BeforeAll
  public static void setUp() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  public static void close() {
    validatorFactory.close();
  }

  @Test
  final void testCountZones() {
    assertEquals(configuredEntries, zoneConfig.zones().size());
  }

  @Test
  final void testZones() {
    Set<ConstraintViolation<AppConfig>> violations = validator.validate(appConfig);
    assertTrue(violations.isEmpty());
    assertEquals(2, zoneConfig.zones().size());
  }

  @Test
  final void testZone_failName() {
    Zone z = new Zone();
    z.setNs("ns1.dyndns.org");
    Set<ConstraintViolation<Zone>> violations = validator.validate(z);
    assertEquals(1, violations.size());
    assertEquals(
        "The name of the zone shouldn't be empty.", violations.iterator().next().getMessage());
  }

  @Test
  final void testZone_failNs() {
    Zone z = new Zone();
    z.setName("test.dyndns.org");
    Set<ConstraintViolation<Zone>> violations = validator.validate(z);
    assertEquals(1, violations.size());
    assertEquals(
        "The primary name server of the zone shouldn't be empty.",
        violations.iterator().next().getMessage());
  }
}
