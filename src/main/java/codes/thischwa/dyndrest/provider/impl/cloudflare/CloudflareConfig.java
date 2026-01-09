package codes.thischwa.dyndrest.provider.impl.cloudflare;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "cloudflare")
@ConfigurationProperties(prefix = "cloudflare")
public record CloudflareConfig(@Nullable String baseUrl, int defaultTtl, String apiKey, String email) {}
