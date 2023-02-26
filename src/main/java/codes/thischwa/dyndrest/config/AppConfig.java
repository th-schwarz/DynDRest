package codes.thischwa.dyndrest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The base configuration of the application.
 */
@ConfigurationProperties(prefix = "dyndrest")
public record AppConfig(String provider, String updateLogFilePattern, int updateLogPageSize, String updateLogPattern,
						String updateLogDatePattern, boolean updateLogPageEnabled, boolean hostValidationEnabled, String updateLogUserName,
						String updateLogUserPassword, boolean updateLogRestForceHttps, boolean greetingEnabled) {
}