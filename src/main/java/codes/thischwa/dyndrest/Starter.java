package codes.thischwa.dyndrest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Starter {

	public static void main(String[] args) {
		try {
			SpringApplication.run(Starter.class, args);
		} catch (Exception e) {
			log.error("Unexpected exception, Spring Boot stops! Message: {}", e.getMessage());
			System.exit(10);
		}
	}
}
