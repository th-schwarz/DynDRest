package codes.thischwa.dyndrest.provider.impl.cloudflare;

import codes.thischwa.dyndrest.AbstractCloudflareTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class CloudflareConfigTest extends AbstractCloudflareTest {

    @Autowired private CloudflareConfig config;

   @Test
   void testConfig() {
       assertEquals("https://api.cloudflare.com/client/v4", config.baseUrl());
       assertEquals("12345678901234567890123456789", config.apiKey());
       assertEquals("YQSn-xWAQiiEh9qM58wZNnyQS7FUdoqGIUAbrh7T", config.apiToken());
       assertEquals("email@mydomain.com", config.email());
   }
}
