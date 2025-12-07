package codes.thischwa.dyndrest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClientResponseException;

@TestPropertySource(properties = {"dyndrest.greeting-enabled=false"})
class GreetingControllerDisabledTest extends AbstractIntegrationTest {

  @Test
  void greetingShouldDisabledTest() {
    try {
      restClient.get()
          .uri("/")
          .retrieve()
          .toEntity(String.class);
    } catch (RestClientResponseException e) {
      HttpStatusCode status = e.getStatusCode();
      assertEquals(HttpStatus.NOT_FOUND, status);
    }
  }
}
