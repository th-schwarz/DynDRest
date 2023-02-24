package codes.thischwa.dyndrest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = { "dyndrest.greeting-enabled=false" })
class GreetingControllerDiabledTest extends GenericIntegrationTest {

	@Test
	void greetingShouldReturnDefaultMessage() throws URISyntaxException {
		HttpStatusCode status = restTemplate.getForEntity(getBaseUri(), String.class).getStatusCode();
		assertTrue(status == HttpStatus.NOT_FOUND || status == HttpStatus.UNAUTHORIZED);
	}
}
