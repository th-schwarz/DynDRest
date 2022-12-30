package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.client.Domainrobot;
import org.domainrobot.sdk.models.DomainRobotHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Configuration
@EnableConfigurationProperties
@Slf4j
public class DomainRobotConfig {

	private static final Map<String, String> customHeaders = new HashMap<>(Map.of(DomainRobotHeaders.DOMAINROBOT_HEADER_WEBSOCKET, "NONE"));

	private final AppConfig appConfig;

	private final AutoDnsConfig autoDnsConfig;

	private final ZoneHostConfig zoneHostConfig;

	@Value("${domainrobot.default-ttl}") private @Getter @Setter int defaultTtl;

	public DomainRobotConfig(AppConfig appConfig, AutoDnsConfig autoDnsConfig, ZoneHostConfig zoneHostConfig) {
		this.appConfig = appConfig;
		this.autoDnsConfig = autoDnsConfig;
		this.zoneHostConfig = zoneHostConfig;
	}

	@Bean
	public Provider provider() {
		final ZoneClientWrapper zcw = buildZoneClientWrapper();
		return new DomainRobotProvider(appConfig, zoneHostConfig, zcw);
	}

	ZoneClientWrapper buildZoneClientWrapper() {
		return new ZoneClientWrapper(
				new Domainrobot(autoDnsConfig.getUser(), String.valueOf(autoDnsConfig.getContext()), autoDnsConfig.getPassword(),
						autoDnsConfig.getUrl()).getZone(), customHeaders, defaultTtl);
	}

}
