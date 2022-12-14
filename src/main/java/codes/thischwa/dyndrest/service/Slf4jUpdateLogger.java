package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Comparator;

/**
 * Implementation of {@link UpdateLogger} that implies an extra log configuration for "UpdateLogger"
 * which logs into an extra file.
 */
@Service
public class Slf4jUpdateLogger implements UpdateLogger, InitializingBean {

	// a hard-coded name is needed for the extra log-appemner
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("UpdateLogger");

	private static final int DEFAULT_HOSTNAME_LENGTH = 12;

	private final Provider provider;

	private final UpdateLogCache cache;

	private String logEntryFormat;

	public Slf4jUpdateLogger(Provider provider, UpdateLogCache cache) {
		this.provider = provider;
		this.cache = cache;
	}

	// it's static, just for testing
	static String buildLogEntry(String logEntryFormat, String host, IpSetting ipSetting) {
		String ipv4 = (ipSetting.getIpv4() == null) ? "n/a" : ipSetting.ipv4ToString();
		String ipv6 = (ipSetting.getIpv6() == null) ? "n/a" : ipSetting.ipv6ToString();
		return String.format(logEntryFormat, host, ipv4, ipv6);
	}

	@Override
	public void log(String host, IpSetting ipSetting) {
		log.info("{}", buildLogEntry(logEntryFormat, host, ipSetting));
		cache.addLogEntry(host, ipSetting.ipv4ToString(), ipSetting.ipv6ToString());
	}

	@Override
	public void afterPropertiesSet() {
		// determine the max. length of the hosts for nicer logging
		int maxSize = provider.getConfiguredHosts().stream().max(Comparator.comparing(String::length)).map(String::length)
				.orElse(DEFAULT_HOSTNAME_LENGTH);
		logEntryFormat = "%" + maxSize + "s  %16s  %s";
	}
}
