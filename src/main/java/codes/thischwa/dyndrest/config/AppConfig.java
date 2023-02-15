package codes.thischwa.dyndrest.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The base configuration of the application.
 */
@Configuration
@ConfigurationProperties(prefix = "dyndrest")
@Slf4j
@Getter
@Setter
public class AppConfig implements InitializingBean {

	private String provider;

	private String updateLogFilePattern;

	private int updateLogPageSize;

	private String updateLogPattern;

	private String updateLogDatePattern;

	private boolean updateLogPageEnabled;

	private boolean hostValidationEnabled = true;

	private String updateLogUserName;

	private String updateLogUserPassword;

	private boolean updateLogRestForceHttps;

	private boolean greetingEnabled;
	
	@Override
	public void afterPropertiesSet() {
		log.info("*** Setting for DynDRest:");
		log.info("  * provider: {}", provider);
		log.info("  * greeting-enabled: {}", greetingEnabled);
		log.info("  * host-validation-enabled: {}", hostValidationEnabled);
		log.info("  * update-log-page-enabled: {}", updateLogPageEnabled);
	}
}