package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class Starter {

	public static void main(String[] args) {
		try {
			SpringApplication.run(Starter.class, args);
		} catch (Exception e) {
			log.error("Unexpected exception, Spring Boot stops! Message: {}", e.getMessage());
			System.exit(10);
		}
	}

	@Component
	static class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

		private final AppConfig config;

		public ApplicationStartup(AppConfig config) {
			this.config = config;
		}

		@Override
		public void onApplicationEvent(final ApplicationReadyEvent event) {
			System.out.println("Application started!");
			log.info("*** Settings for DynDRest:");
			log.info("  * provider: {}", config.provider());
			log.info("  * greeting-enabled: {}", config.greetingEnabled());
			log.info("  * host-validation-enabled: {}", config.hostValidationEnabled());
			log.info("  * update-log-page-enabled: {}", config.updateLogPageEnabled());
		}
	}
}
