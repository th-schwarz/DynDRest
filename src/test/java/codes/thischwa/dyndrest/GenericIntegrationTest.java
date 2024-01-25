package codes.thischwa.dyndrest;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class GenericIntegrationTest {

	@Value("${local.server.port}") protected int port;

	@Autowired protected TestRestTemplate restTemplate;

	String getBaseUrl() {
		return "http://localhost:" + port + "/";
	}

	URI getBaseUri() throws URISyntaxException {
		return new URI(getBaseUrl());
	}
}
