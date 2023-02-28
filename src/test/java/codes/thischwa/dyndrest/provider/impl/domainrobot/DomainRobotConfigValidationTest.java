package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
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

	@Autowired private DomainRobotConfig config;

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
		assertEquals(configuredEntries, config.zones().size());
	}

	@Test
	final void testZoneDetails() {
		DomainRobotConfig.Zone zone = config.zones().get(0);
		assertEquals("dynhost0.info", zone.name());
		assertEquals("ns0.domain.info", zone.ns());

		assertEquals("my0:1234567890abcdef", zone.hosts().get(0));
		assertEquals("test0:1234567890abcdx", zone.hosts().get(1));
	}

	@Test
	final void testZones() {
		Set<ConstraintViolation<DomainRobotConfig>> violations = validator.validate(config);
		assertTrue(violations.isEmpty());
		assertEquals(2, config.zones().size());
	}

	@Test
	final void testZone_failName() {
		DomainRobotConfig.Zone z = new DomainRobotConfig.Zone(null,"ns.dyndns.org", Arrays.asList("test1", "test2"));
		Set<ConstraintViolation<DomainRobotConfig.Zone>> violations = validator.validate(z);
		assertEquals(1, violations.size());
		assertEquals("The name of the zone shouldn't be empty.", violations.iterator().next().getMessage());
	}

	@Test
	final void testZone_failNs() {
		DomainRobotConfig.Zone z = new DomainRobotConfig.Zone("test.dyndns.org",null, Arrays.asList("test1", "test2"));
		Set<ConstraintViolation<DomainRobotConfig.Zone>> violations = validator.validate(z);
		assertEquals(1, violations.size());
		assertEquals("The primary name server of the zone shouldn't be empty.", violations.iterator().next().getMessage());
	}

	@Test
	final void testZone_failHosts() {
		DomainRobotConfig.Zone z = new DomainRobotConfig.Zone("test.dyndns.org","ns.dyndns.org", null);
		Set<ConstraintViolation<DomainRobotConfig.Zone>> violations = validator.validate(z);
		assertEquals(1, violations.size());
		assertEquals("The hosts of the zone shouldn't be empty.", violations.iterator().next().getMessage());
	}


}