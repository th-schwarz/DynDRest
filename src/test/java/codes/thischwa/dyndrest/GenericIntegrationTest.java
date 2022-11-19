package codes.thischwa.dyndrest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
