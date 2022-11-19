package codes.thischwa.dyndrest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = { "dyndrest.greeting-enabled=false" })
class GreetingControllerDiabledTest extends GenericIntegrationTest {

	@Test
	void greetingShouldReturnDefaultMessage() throws URISyntaxException {
		assertEquals(HttpStatus.NOT_FOUND, restTemplate.getForEntity(getBaseUri(), String.class).getStatusCode());
	}
}
