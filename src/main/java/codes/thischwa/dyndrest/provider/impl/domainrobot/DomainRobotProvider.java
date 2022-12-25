package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.GenericProvider;
import codes.thischwa.dyndrest.provider.ProviderException;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;

import java.util.Set;

@Slf4j
public class DomainRobotProvider extends GenericProvider {

	private final AppConfig appConfig;

	private final ZoneHostConfig zoneHostConfig;

	private final ZoneClientWrapper zcw;

	DomainRobotProvider(AppConfig appConfig, ZoneHostConfig zoneHostConfig, ZoneClientWrapper zcw) {
		this.appConfig = appConfig;
		this.zoneHostConfig = zoneHostConfig;
		this.zcw = zcw;
	}

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
}
