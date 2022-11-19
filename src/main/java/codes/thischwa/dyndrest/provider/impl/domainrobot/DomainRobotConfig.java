package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.GenericProvider;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.client.Domainrobot;
import org.domainrobot.sdk.models.DomainRobotHeaders;
import org.domainrobot.sdk.models.generated.Zone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Configuration
@EnableConfigurationProperties
@Slf4j
public class DomainRobotConfig {

	private static final Map<String, String> customHeaders = new HashMap<>(Map.of(DomainRobotHeaders.DOMAINROBOT_HEADER_WEBSOCKET, "NONE"));

	private final AppConfig appConfig;

	private final AutoDnsConfig autoDnsConfig;

	private final ZoneHostConfig zoneHostConfig;

	@Value("${domainrobot.default-ttl}")
	private @Getter @Setter int defaultTtl;

	public DomainRobotConfig(AppConfig appConfig, AutoDnsConfig autoDnsConfig, ZoneHostConfig zoneHostConfig) {
		this.appConfig = appConfig;
		this.autoDnsConfig = autoDnsConfig;
		this.zoneHostConfig = zoneHostConfig;
	}

	@Bean
	public Provider provider() {
		return new GenericProvider() {

			private final ZoneClientWrapper zcw = new ZoneClientWrapper(new Domainrobot(autoDnsConfig.getUser(), String.valueOf(autoDnsConfig.getContext()), autoDnsConfig.getPassword(),
						autoDnsConfig.getUrl()).getZone(), customHeaders, defaultTtl);

			@Override
			public void validateHostConfiguration() throws IllegalArgumentException {
				if(appConfig.isHostValidationEnabled()) {
					zoneHostConfig.getConfiguredZones().forEach(this::checkZone);
				}
			}

			@Override
			public void update(String host, IpSetting ipSetting) throws ProviderException {
				String sld = host.substring(0, host.indexOf("."));

				// set the IPs in the zone object
				Zone zone = zoneInfo(host);
				if(!zcw.hasIPsChanged(zone, sld, ipSetting.getIpv4(), ipSetting.getIpv6()))
					return;
				zcw.processIpSetting(zone, sld, ipSetting);

				// processing the update
				zcw.update(zone);
			}

			private void checkZone(String zoneStr) throws IllegalArgumentException {
				try {
					Zone zone = zcw.info(zoneStr, zoneHostConfig.getPrimaryNameServer(zoneStr));
					log.info("*** Zone confirmed: {}", zone.getOrigin());
				} catch (ProviderException e) {
					log.error("Error while getting zone info of " + zoneStr, e);
					throw new IllegalArgumentException("Zone couldn't be confirmed.");
				}
			}

			@Override
			public Set<String> getConfiguredHosts() {
				return zoneHostConfig.getConfiguredHosts();
			}

			@Override
			public String getApitoken(String host) {
				return zoneHostConfig.getApitoken(host);
			}

			Zone zoneInfo(String host) throws ProviderException, IllegalArgumentException {
				if(!zoneHostConfig.hostExists(host))
					throw new IllegalArgumentException("Host isn't configured: " + host);
				String zone = zcw.deriveZone(host);
				String primaryNameServer = zoneHostConfig.getPrimaryNameServer(zone);
				return zcw.info(zone, primaryNameServer);
			}
		};
	}

}
