package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.config.ZoneConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainRobotConfigValidationTest extends GenericIntegrationTest {

	private final int configuredEntries = 2;

	private static Validator validator;

	private static ValidatorFactory validatorFactory;

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
	final void testZoneDetails() {
		ZoneConfig.Zone zone = zoneConfig.zones().get(0);
		assertEquals("dynhost0.info", zone.name());
		assertEquals("ns0.domain.info", zone.ns());

		assertEquals("my0:1234567890abcdef", zone.hosts().get(0));
		assertEquals("test0:1234567890abcdx", zone.hosts().get(1));
	}

	@Test
	final void testZones() {
		Set<ConstraintViolation<AppConfig>> violations = validator.validate(appConfig);
		assertTrue(violations.isEmpty());
		assertEquals(2, zoneConfig.zones().size());
	}

	@Test
	final void testZone_failName() {
		ZoneConfig.Zone z = new ZoneConfig.Zone(null,"ns.dyndns.org", Arrays.asList("test1", "test2"));
		Set<ConstraintViolation<ZoneConfig.Zone>> violations = validator.validate(z);
		assertEquals(1, violations.size());
		assertEquals("The name of the zone shouldn't be empty.", violations.iterator().next().getMessage());
	}

	@Test
	final void testZone_failNs() {
		ZoneConfig.Zone z = new ZoneConfig.Zone("test.dyndns.org",null, Arrays.asList("test1", "test2"));
		Set<ConstraintViolation<ZoneConfig.Zone>> violations = validator.validate(z);
		assertEquals(1, violations.size());
		assertEquals("The primary name server of the zone shouldn't be empty.", violations.iterator().next().getMessage());
	}

	@Test
	final void testZone_failHosts() {
		ZoneConfig.Zone z = new ZoneConfig.Zone("test.dyndns.org","ns.dyndns.org", null);
		Set<ConstraintViolation<ZoneConfig.Zone>> violations = validator.validate(z);
		assertEquals(1, violations.size());
		assertEquals("The hosts of the zone shouldn't be empty.", violations.iterator().next().getMessage());
	}


}