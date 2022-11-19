package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.provider.impl.domainrobot.ZoneHostConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigValidationTest extends GenericIntegrationTest {

	private static Validator validator;

	private static ValidatorFactory validatorFactory;

	@Autowired private ZoneHostConfig config;

	@BeforeAll
	public static void setUp() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	public static void close() {
		validatorFactory.close();
	}

	static ZoneHostConfig.Zone buildZone() {
		ZoneHostConfig.Zone z = new ZoneHostConfig.Zone();
		z.setName("test.dyndns.org");
		z.setNs("ns.dyndns.org");
		z.setHosts(Arrays.asList("test1", "test2"));
		return z;
	}

	@Test
	final void testZones() {
		Set<ConstraintViolation<ZoneHostConfig>> violations = validator.validate(config);
		assertTrue(violations.isEmpty());
		assertEquals(2, config.getZones().size());
	}

	@Test
	final void testZone_failName() {
		ZoneHostConfig.Zone z = buildZone();
		z.setName(null);
		Set<ConstraintViolation<ZoneHostConfig.Zone>> violations = validator.validate(z);
		assertEquals(1, violations.size());
		assertEquals("The name of the zone shouldn't be empty.", violations.iterator().next().getMessage());
	}
}
