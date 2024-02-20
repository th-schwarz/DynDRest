package codes.thischwa.dyndrest;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

	@Value("${local.server.port}") protected int port;

	@Autowired protected TestRestTemplate restTemplate;

	String getBaseUrl() {
		return "http://localhost:" + port + "/";
	}

	URI getBaseUri() throws URISyntaxException {
		return new URI(getBaseUrl());
	}
}
