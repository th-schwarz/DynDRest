package codes.thischwa.dyndrest.provider.impl.cloudflare;

import codes.thischwa.cf.CfDnsClient;
import codes.thischwa.cf.CloudflareApiException;
import codes.thischwa.cf.CloudflareNotFoundException;
import codes.thischwa.cf.model.RecordEntity;
import codes.thischwa.cf.model.RecordType;
import codes.thischwa.cf.model.ZoneEntity;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.util.NetUtil;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;

/** Implementation for the Cloudflare API */
@Slf4j
public class CloudflareProvider extends GenericProvider implements InitializingBean {

  private final AppConfig appConfig;
  private final HostZoneService hostZoneService;
  private final CfDnsClient cfDnsClient;
  private final int defaultTtl;

  CloudflareProvider(
      AppConfig appConfig, CloudflareConfig config, HostZoneService hostZoneService) {
    this.appConfig = appConfig;
    this.hostZoneService = hostZoneService;
    this.defaultTtl = config.defaultTtl();
    cfDnsClient =
        new CfDnsClient(config.baseUrl(), config.email(), config.apiKey());
  }

  @Override
  public void validateHostZoneConfiguration() throws IllegalArgumentException {
    if (appConfig.hostValidationEnabled()) {
      hostZoneService.getConfiguredZones().forEach(this::zoneConfirmed);
    }
  }

  @Override
  public void update(String host, IpSetting ipSetting) throws ProviderException {
    ZoneEntity zone = fetchZoneFromHost(host);
    String sld = getSldFromHost(host);
    try {
      boolean updated =
          sldCreateUpdateOrDeleteIp(zone, sld, ipSetting.getIpv4(), ipSetting.getIpv6());
      if (!updated) {
        log.info("*** No update required for host: {}", host);
      }
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public void addHost(String zoneName, String host) throws ProviderException {
    // not required for domainrobot. #update adds the required records.
  }

  @Override
  public void removeHostIpSettings(String host) throws ProviderException {
    ZoneEntity zone = fetchZoneFromHost(host);
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(host);
    if (optFullHost.isEmpty()) {
      throw new ProviderException("Host isn't configured: " + host);
    }
    try {
      sldDeleteIpSettings(zone, host);
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  /**
   * Deletes specified DNS records of types A and AAAA if they exist for a given zone and subdomain.
   *
   * @param zone The zone entity that represents the DNS zone where the records should be deleted.
   * @param sld The second-level domain (subdomain) for which the DNS records should be deleted.
   * @throws CloudflareApiException If an error occurs while interacting with the Cloudflare API.
   */
  private void sldDeleteIpSettings(ZoneEntity zone, String sld) throws CloudflareApiException {
    cfDnsClient.recordDeleteTypeIfExists(zone, sld, RecordType.A);
    cfDnsClient.recordDeleteTypeIfExists(zone, sld, RecordType.AAAA);
  }

  private void zoneConfirmed(Zone myZone) throws IllegalArgumentException {
    ZoneEntity zone;
    try {
      zone = cfDnsClient.zoneInfo(myZone.getName());
      log.info("*** Zone confirmed: {}", zone.getName());
      hostsOfZoneConfirmed(zone);
    } catch (CloudflareApiException | ProviderException e) {
      log.error("Error while getting zone info of {}", myZone.getName(), e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
  }

  private void hostsOfZoneConfirmed(ZoneEntity zone)
      throws IllegalArgumentException, ProviderException {
    Optional<List<HostEnriched>> opt = hostZoneService.findHostsOfZone(zone.getName());
    if (opt.isEmpty()) {
      log.warn("No hosts found for zone: {}", zone.getName());
      return;
    }
    for (HostEnriched host : opt.get()) {
      try {
        if (hasSubTld(cfDnsClient, zone, host.getFullHost())) {
          log.info("Host confirmed: {}", host.getFullHost());
        } else {
          throw new IllegalArgumentException("Host not confirmed: " + host.getFullHost());
        }
      } catch (CloudflareApiException e) {
        throw new ProviderException(e);
      }
    }
  }

  /**
   * Zone info zone.
   *
   * @param host the host
   * @return the zone
   * @throws ProviderException the provider exception
   * @throws IllegalArgumentException the illegal argument exception
   */
  ZoneEntity fetchZoneFromHost(String host) throws ProviderException, IllegalArgumentException {
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(host);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host isn't configured: " + host);
    }
    HostEnriched hostEnriched = optFullHost.get();
    String zone = hostEnriched.getZone();
    try {
      return cfDnsClient.zoneInfo(zone);
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    validateHostZoneConfiguration();
  }

  private String getSldFromHost(String host) {
    return host.substring(0, host.indexOf("."));
  }

  /**
   * Creates, updates, or deletes DNS A and AAAA records for a given second-level domain (SLD) in a
   * specified zone. If either the IPv4 or IPv6 address is null, the corresponding DNS record will
   * be deleted.
   *
   * @param zone The ZoneEntity object representing the DNS zone where the operation is performed.
   * @param sld The second-level domain (SLD) for which the DNS record is being managed.
   * @param ipv4 An optional IPv4 address for the DNS A record. If null, the A record will be
   *     deleted.
   * @param ipv6 An optional IPv6 address for the DNS AAAA record. If null, the AAAA record will be
   *     deleted.
   * @return A boolean indicating whether any DNS record was created, updated, or deleted.
   * @throws CloudflareApiException If an error occurs during the operation with the Cloudflare API.
   */
  private boolean sldCreateUpdateOrDeleteIp(
      ZoneEntity zone, String sld, @Nullable Inet4Address ipv4, @Nullable Inet6Address ipv6)
      throws CloudflareApiException {
    boolean updated = false;
    String ipStr = ipv4 != null ? ipv4.getHostAddress() : null;
    updated |= sldCreateUpdateOrDeleteIp(zone, sld, ipStr, RecordType.A);
    ipStr = ipv6 != null ? ipv6.getHostAddress() : null;
    updated |= sldCreateUpdateOrDeleteIp(zone, sld, ipStr, RecordType.AAAA);
    return updated;
  }

  private boolean sldCreateUpdateOrDeleteIp(
      ZoneEntity zone, String sld, @Nullable String ip, RecordType type)
      throws CloudflareApiException {
    try {
      RecordEntity rec = cfDnsClient.sldInfo(zone, sld, type);
      if (Objects.isNull(ip)) {
        cfDnsClient.recordDelete(zone, rec);
        return true;
      }
      if (!NetUtil.ipEquals(rec.getContent(), ip)) {
        rec.setContent(ip);
        cfDnsClient.recordUpdate(zone, rec);
        return true;
      }
    } catch (CloudflareNotFoundException e) {
      if (ip != null) {
        cfDnsClient.recordCreate(zone, RecordEntity.build(sld, type, defaultTtl, ip));
        log.debug("Created new record successful for host {} of type {} with IP {}", sld, type, ip);
        return true;
      }
    }
    return false;
  }

  private boolean hasSubTld(CfDnsClient client, ZoneEntity zone, String host)
      throws CloudflareApiException {
    String sld = getSldFromHost(host);
    boolean aFound = false;
    try {
      client.sldInfo(zone, sld, RecordType.A);
      aFound = true;
    } catch (CloudflareApiException e) {
      if (!(e instanceof CloudflareNotFoundException)) {
        log.error("Unexpected error while getting A record of host {}", host, e);
        throw e;
      }
    }

    boolean aaaaFound = false;
    try {
      client.sldInfo(zone, sld, RecordType.AAAA);
      aaaaFound = true;
    } catch (CloudflareApiException e) {
      if (!(e instanceof CloudflareNotFoundException)) {
        log.error("Unexpected error while getting AAAA record of host {}", host, e);
        throw e;
      }
    }
    return aFound || aaaaFound;
  }
}
