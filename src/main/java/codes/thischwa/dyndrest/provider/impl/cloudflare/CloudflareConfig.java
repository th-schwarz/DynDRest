package codes.thischwa.dyndrest.provider.impl.cloudflare;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties specific to the Cloudflare integration in the application.
 * This class is activated conditionally based on the `dyndrest.provider` property
 * having the value `cloudflare`.
 *
 * <p>The configuration for this class is prefixed with `cloudflare`, allowing users
 * to define relevant properties specific to the Cloudflare provider in their
 * application configuration.
 *
 * @param baseUrl         The base URL for the Cloudflare API. This parameter is optional and can be null.
 * @param defaultTtl      The default time-to-live (TTL) value for DNS records, defined in seconds.
 * @param connectTimeout  The timeout for establishing a connection to the Cloudflare API.
 * @param responseTimeout The timeout for waiting for a response from the Cloudflare API.
 * @param email           The email associated with the Cloudflare account. Required for API authentication.
 * @param apiKey          The API key used for authenticating with the Cloudflare API.
 * @param apiToken        The API token used for authenticating with the Cloudflare API.
 */
@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "cloudflare")
@ConfigurationProperties(prefix = "cloudflare")
public record CloudflareConfig(@Nullable String baseUrl, int defaultTtl, @Nullable Duration connectTimeout,
                               @Nullable Duration responseTimeout, @Nullable String email, @Nullable String apiKey,
                               @Nullable String apiToken) {
  boolean isApiTokenConfigured() {
    return apiToken != null;
  }
}
