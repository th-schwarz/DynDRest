package codes.thischwa.dyndrest.provider.impl.cloudflare;

import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractCloudflareTest;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CloudflareConfigTest extends AbstractCloudflareTest {

  @Autowired private CloudflareConfig config;

  @Test
  void testConfig() {
    assertEquals("https://api.cloudflare.com/client/v4", config.baseUrl());
    assertEquals("12345678901234567890123456789", config.apiKey());
    assertEquals("email@mydomain.com", config.email());
    assertEquals("98765432109876543210987654321098", config.apiToken());
    assertEquals(Duration.ofSeconds(10), config.connectTimeout());
    assertEquals(Duration.ofSeconds(30), config.responseTimeout());
  }
}
