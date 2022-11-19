package codes.thischwa.dyndrest.provider.impl.domainrobot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ConditionalOnProperty(name = "dyndrest.provider", havingValue = "domainrobot")
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "domainrobot")
@Slf4j
public class ZoneHostConfig implements InitializingBean {

	// <zone, ns>
	private Map<String, String> zoneData = null;

	// <fqdn, apitoken>
	private Map<String, String> apitokenData = null;

	@NotEmpty(message = "The zones of the AutoDNS configuration shouldn't be empty.") private @Getter @Setter List<Zone> zones;

	public Set<String> getConfiguredHosts() {
		return apitokenData.keySet();
	}

	public Set<String> getConfiguredZones() {
		return zoneData.keySet();
	}

	public boolean hostExists(String host) {
		return apitokenData.containsKey(host);
	}

	public String getApitoken(String host) throws IllegalArgumentException {
		if(!hostExists(host))
			throw new IllegalArgumentException("Host isn't configured: " + host);
		return apitokenData.get(host);
	}

	public String getPrimaryNameServer(String zone) throws IllegalArgumentException {
		if(!zoneData.containsKey(zone))
			throw new IllegalArgumentException("Zone isn't configured: " + zone);
		return zoneData.get(zone);
	}

	@Override
	public void afterPropertiesSet() {
		readAndValidate();
		log.info("*** Api-token and zone data are read and validated successful!");
	}

	void readAndValidate() {
		read();
		validate();
	}

	void read() throws IllegalArgumentException {
		apitokenData = new HashMap<>();
		zoneData = new HashMap<>();
		zones.forEach(this::readZoneConfig);
	}
	private void readZoneConfig(ZoneHostConfig.Zone zone) {
		zoneData.put(zone.getName(), zone.getNs());
		List<String> hostRawData = zone.getHosts();
		if(hostRawData == null || hostRawData.isEmpty())
			throw new IllegalArgumentException("Missing host data for: " + zone.getName());
		hostRawData.forEach(hostRaw -> readHostString(hostRaw, zone));
	}

	private void readHostString(String hostRaw, Zone zone) {
		String[] parts = hostRaw.split(":");
		if(parts.length != 2)
			throw new IllegalArgumentException(
					"The host entry must be in the following format: [sld|:[apitoken], but it was: " + hostRaw);
		// build the fqdn hostname
		String host = String.format("%s.%s", parts[0], zone.getName());
		apitokenData.put(host, parts[1]);
	}

	void validate() {
		if(zoneData == null || zoneData.isEmpty() || apitokenData == null || apitokenData.isEmpty())
			throw new IllegalArgumentException("Zone or host data are empty.");
		log.info("*** Configured hosts:");
		apitokenData.keySet().forEach(host -> log.info(" - {}", host));
	}

	public static class Zone {

		@NotBlank(message = "The name of the zone shouldn't be empty.") private @Getter @Setter String name;

		@NotBlank(message = "The primary name server of the zone shouldn't be empty.") private @Getter @Setter String ns;

		// is validated by DDAutoContext#readData
		private @Getter List<String> hosts;

		public void setHosts(@Valid List<String> host) {
			this.hosts = host;
		}

	}
}
