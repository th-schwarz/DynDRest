package codes.thischwa.dyndrest.provider.impl.cloudflare;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "cloudflare")
@ConfigurationProperties(prefix = "cloudflare")
record CloudflareConfig(String baseUrl, String apiToken, String apiKey, String email) {}
