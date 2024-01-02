package codes.thischwa.dyndrest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = { "dyndrest.greeting-enabled=false" })
class GreetingControllerDisabledTest extends GenericIntegrationTest {

	@Test
	void greetingShouldReturnDefaultMessage() throws URISyntaxException {
		HttpStatusCode status = restTemplate.getForEntity(getBaseUri(), String.class).getStatusCode();
		assertEquals(status, HttpStatus.UNAUTHORIZED);
	}
}
