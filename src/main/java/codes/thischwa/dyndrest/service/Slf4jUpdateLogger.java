package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.IpSetting;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UpdateLogger} that implies an extra log configuration for "UpdateLogger"
 * which logs into an extra file.
 */
@Service
@ConditionalOnProperty(name = "dyndrest.update-log-page-enabled", havingValue = "true")
public class Slf4jUpdateLogger implements UpdateLogger, InitializingBean {

  // a hard-coded name is needed for the extra log-appender
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("UpdateLogger");

  private static final int DEFAULT_HOSTNAME_LENGTH = 12;

  private final HostZoneService hostZoneService;

  private final UpdateLogCache cache;

  private String logEntryFormat;

  public Slf4jUpdateLogger(HostZoneService hostZoneService, UpdateLogCache cache) {
    this.hostZoneService = hostZoneService;
    this.cache = cache;
    this.logEntryFormat = "%" + DEFAULT_HOSTNAME_LENGTH + "s  %16s  %s";
  }

  // it's static, just for testing
  static String buildLogEntry(String logEntryFormat, String host, IpSetting ipSetting) {
    String ipv4 = (ipSetting.getIpv4() == null) ? "n/a" : ipSetting.ipv4ToString();
    String ipv6 = (ipSetting.getIpv6() == null) ? "n/a" : ipSetting.ipv6ToString();
    return String.format(logEntryFormat, host, ipv4, ipv6);
  }

  @Override
  public void log(String host, IpSetting ipSetting) {
    log.info(buildLogEntry(logEntryFormat, host, ipSetting));
    cache.addLogItem(host, ipSetting.ipv4ToString(), ipSetting.ipv6ToString());
  }

  @Override
  public void afterPropertiesSet() {
    // determine the max. length of the hosts for nicer logging
    int maxSize = hostZoneService.getConfiguredHosts().stream()
              .map(FullHost::getFullHost)
              .mapToInt(String::length)
              .max()
              .orElse(DEFAULT_HOSTNAME_LENGTH);
    logEntryFormat = "%" + maxSize + "s  %16s  %s";
  }
}
