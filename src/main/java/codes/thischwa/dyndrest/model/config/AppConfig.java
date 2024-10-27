package codes.thischwa.dyndrest.model.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

/** The base configuration of the application. */
@ConfigurationProperties(prefix = "dyndrest")
public record AppConfig(
    String provider,
    boolean greetingEnabled,
    boolean hostValidationEnabled,
    int updateIpChangedStatus,
    int updateLogPageSize,
    String updateLogDatePattern,
    boolean updateLogPageEnabled,
    @Nullable String updateLogUserName,
    @Nullable String updateLogUserPassword,
    boolean updateLogRestForceHttps,
    @Nullable String healthCheckUserName,
    @Nullable String healthCheckUserPassword,
    @Nullable String adminUserName,
    @Nullable String adminUserPassword,
    @Nullable String adminApiToken) {

}
