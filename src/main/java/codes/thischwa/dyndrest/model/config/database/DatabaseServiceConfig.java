package codes.thischwa.dyndrest.model.config.database;


import org.springframework.boot.context.properties.ConfigurationProperties;

/** Represents a configuration class for a database connection. */
@ConfigurationProperties(prefix = "dyndrest.database")
public record DatabaseServiceConfig(
        String dumpFile) {
}
