package codes.thischwa.dyndrest.provider.impl.cloudflare;

import codes.thischwa.cf.CfDnsClient;
import codes.thischwa.cf.CloudflareApiException;
import codes.thischwa.cf.model.RecordEntity;
import codes.thischwa.cf.model.RecordType;
import codes.thischwa.cf.model.ZoneEntity;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import codes.thischwa.dyndrest.util.NetUtil;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation for the Cloudflare API.
 */
@Slf4j
public class CloudflareProvider extends GenericProvider implements InitializingBean {

  CfDnsClient cfDnsClient;
  private final int defaultTtl;

  CloudflareProvider(
      AppConfig appConfig, CloudflareConfig config, HostZoneService hostZoneService, ZoneUpdaterService zoneUpdaterService,
      DynamicSecurityChainManager securityChainManager) {
    super(appConfig, securityChainManager, hostZoneService, zoneUpdaterService);
    this.defaultTtl = config.defaultTtl();
    if (config.baseUrl() != null) {
      cfDnsClient = new CfDnsClient(config.baseUrl(), config.email(), config.apiKey());
    } else {
      cfDnsClient = new CfDnsClient(config.email(), config.apiKey());
    }
  }

  @Override
  public void addOrUpdate(String fqdn, IpSetting ipSetting) throws ProviderException {
    ZoneEntity zone = fetchZoneFromHost(fqdn);
    String sld = getSldFromHost(fqdn);
    try {
      boolean updated =
          sldCreateUpdateOrDeleteIp(zone, sld, ipSetting.getIpv4(), ipSetting.getIpv6());
      if (!updated) {
        log.info("*** No addOrUpdate required for host: {}", fqdn);
      }
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public IpSetting info(String fqdn) throws ProviderException {
    ZoneEntity zone = fetchZoneFromHost(fqdn);
    String sld = getSldFromHost(fqdn);
    IpSetting ipSetting = new IpSetting();
    try {
      List<RecordEntity> arecords = cfDnsClient.recordList(zone, sld, RecordType.A);
      if (!arecords.isEmpty()) {
        ipSetting.setIpv4(arecords.get(0).getContent());
      }
    } catch (CloudflareApiException e) {
      log.warn("Error while getting A record of host {}", fqdn, e);
    }
    try {
      List<RecordEntity> aaaaRecords = cfDnsClient.recordList(zone, sld, RecordType.AAAA);
      if (!aaaaRecords.isEmpty()) {
        ipSetting.setIpv6(aaaaRecords.get(0).getContent());
      }
    } catch (CloudflareApiException e) {
      log.warn("Error while getting AAAA record of host {}", fqdn, e);
    }
    return ipSetting;
  }

  @Override
  public void removeHostIpSettings(String fqdn) throws ProviderException {
    ZoneEntity zone;
    try {
      zone = fetchZoneFromHost(fqdn);
    } catch (IllegalArgumentException e) {
      throw new ProviderException(e);
    }
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(fqdn);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host isn't configured: " + fqdn);
    }
    String sld = getSldFromHost(fqdn);
    try {
      sldDeleteIpSettings(zone, sld);
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public void patch(String zone, @Nullable List<HostInfoHolder> creates, @Nullable List<HostInfoHolder> updates,
                    @Nullable List<String> deletes) throws ProviderException {
    ZoneEntity zoneEntity;
    try {
      zoneEntity = cfDnsClient.zoneGet(zone);
    } catch (CloudflareApiException e) {
      log.error("Error while getting zone info of {}", zone, e);
      throw new ProviderException(e);
    }

    List<RecordEntity> recordsToCreate;
    if (creates == null) {
      recordsToCreate = null;
    } else {
      recordsToCreate = new ArrayList<>();
      creates.forEach(record -> {
        IpSetting ipSetting = record.getIpSetting();
        if (ipSetting.getIpv4() != null) {
          recordsToCreate.add(convert(record.getSld(), RecordType.A, ipSetting.getIpv4()));
        }
        if (ipSetting.getIpv6() != null) {
          recordsToCreate.add(convert(record.getSld(), RecordType.AAAA, ipSetting.getIpv6()));
        }
      });
    }
    List<RecordEntity> recordsToUpdate;
    if (updates == null) {
      recordsToUpdate = null;
    } else {
      recordsToUpdate = new ArrayList<>();
      updates.forEach(rec -> {
        IpSetting ipSetting = rec.getIpSetting();
        if (ipSetting.getIpv4() != null) {
          recordsToUpdate.add(convert(rec.getSld(), RecordType.A, ipSetting.getIpv4()));
        }
        if (ipSetting.getIpv6() != null) {
          recordsToUpdate.add(convert(rec.getSld(), RecordType.AAAA, ipSetting.getIpv6()));
        }
      });
    }

    try {
      cfDnsClient.recordBatch(zoneEntity, recordsToCreate, recordsToUpdate, null, null);
    } catch (CloudflareApiException e) {
      throw new ProviderException(e);
    }
  }

  private RecordEntity convert(String sld, RecordType recordType, InetAddress ip) {
    RecordEntity rec = new RecordEntity();
    rec.setTtl(defaultTtl);
    rec.setType(recordType.getType());
    rec.setName(sld);
    rec.setContent(ip.getHostAddress());
    return rec;
  }

  /**
   * Deletes specified DNS records of types A and AAAA if they exist for a given zone and subdomain.
   *
   * @param zone The zone entity that represents the DNS zone where the records should be deleted.
   * @param sld  The second-level domain (subdomain) for which the DNS records should be deleted.
   * @throws CloudflareApiException If an error occurs while interacting with the Cloudflare API.
   */
  private void sldDeleteIpSettings(ZoneEntity zone, String sld) throws CloudflareApiException {
    cfDnsClient.recordDeleteTypeIfExists(zone, sld, RecordType.A, RecordType.AAAA);
  }

  @Override
  public void confirmZone(Zone myZone) throws IllegalArgumentException {
    ZoneEntity zone;
    try {
      zone = cfDnsClient.zoneGet(myZone.getName());
      log.info("*** Zone confirmed: {}", zone.getName());
      hostsOfZoneConfirmed(zone);
    } catch (CloudflareApiException | ProviderException e) {
      log.error("Error while getting zone info of {}", myZone.getName(), e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
  }

  @Override
  public List<HostInfoHolder> getCurrentConfiguredHosts(List<HostEnriched> hostsEnriched) throws ProviderException {
    Map<String, ZoneEntity> knownRealZones = new HashMap<>();
    Map<String, HostEnriched> hostsByString = new HashMap<>();
    List<HostInfoHolder> hosts = new ArrayList<>();

    // collect base data
    for (HostEnriched host : hostsEnriched) {
      hostsByString.put(host.getFullHost(), host);
      if (!knownRealZones.containsKey(host.getZone())) {
        try {
          ZoneEntity zone = cfDnsClient.zoneGet(host.getZone());
          knownRealZones.put(host.getZone(), zone);
        } catch (CloudflareApiException e) {
          throw new ProviderException("Failed to get zone: " + host.getZone(), e);
        }
      }
    }

    // fetch data from cloudflare
    for (ZoneEntity zone : knownRealZones.values()) {
      Map<String, IpSetting> result = fetchIpSettingOfZone(zone, hostsByString.keySet());
      for (String fqdn : result.keySet()) {
        HostEnriched host = hostsByString.get(fqdn);
        IpSetting ipSetting = result.get(fqdn);
        hosts.add(HostInfoHolder.of(host, ipSetting));
      }
    }

    return hosts;
  }

  private Map<String, IpSetting> fetchIpSettingOfZone(ZoneEntity zone, Set<String> relevantHosts) {
    Map<String, IpSetting> result = new HashMap<>();
    List<RecordEntity> records;
    try {
      records = cfDnsClient.recordList(zone, RecordType.A, RecordType.AAAA);
    } catch (CloudflareApiException e) {
      log.error("Error while getting records of zone {}", zone.getName(), e);
      throw new RuntimeException(e);
    }

    Map<String, List<RecordEntity>> recordsByHost = CfDnsClient.groupRecordsByFqdn(records);
    for (Map.Entry<String, List<RecordEntity>> recByHost : recordsByHost.entrySet()) {
      String fqdn = recByHost.getKey();
      if (relevantHosts.contains(fqdn)) {
        IpSetting ipSetting = new IpSetting();
        List<RecordEntity> recs = recByHost.getValue();
        if (recs.isEmpty()) {
          continue;
        }
        String ipv4Str = null;
        String ipv6Str = null;
        for (RecordEntity rec : recs) {
          if (RecordType.A.getType().equals(rec.getType())) {
            ipv4Str = rec.getContent();
          } else if (RecordType.AAAA.getType().equals(rec.getType())) {
            ipv6Str = rec.getContent();
          }
        }
        ipSetting.setIpv4(ipv4Str);
        ipSetting.setIpv6(ipv6Str);
        result.put(fqdn, ipSetting);
      }
    }
    return result;
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
   * Retrieves the associated DNS zone for the given fully qualified domain name (FQDN).
   *
   * @param fqdn The fully qualified domain name for which the DNS zone should be retrieved.
   *             Must be a valid, non-null string.
   * @return The ZoneEntity object representing the DNS zone associated with the given FQDN.
   * @throws ProviderException        If an error occurs while interacting with the Cloudflare API.
   * @throws IllegalArgumentException If the specified FQDN cannot be found.
   */
  ZoneEntity fetchZoneFromHost(String fqdn) throws ProviderException, IllegalArgumentException {
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(fqdn);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host can't be found: " + fqdn);
    }
    HostEnriched hostEnriched = optFullHost.get();
    String zone = hostEnriched.getZone();
    try {
      return cfDnsClient.zoneGet(zone);
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
   * @param sld  The second-level domain (SLD) for which the DNS record is being managed.
   * @param ipv4 An optional IPv4 address for the DNS A record. If null, the A record will be
   *             deleted.
   * @param ipv6 An optional IPv6 address for the DNS AAAA record. If null, the AAAA record will be
   *             deleted.
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
    List<RecordEntity> recs = cfDnsClient.recordList(zone, sld, type);
    if (recs.isEmpty()) {
      if (ip != null) {
        cfDnsClient.recordCreate(zone, RecordEntity.build(sld, type, defaultTtl, ip));
        log.debug("Created new record successful for host {} of type {} with IP {}", sld, type, ip);
        return true;
      }
      return false;
    }

    RecordEntity rec = recs.get(0);
    if (Objects.isNull(ip)) {
      cfDnsClient.recordDelete(zone, rec);
      return true;
    }
    if (!NetUtil.ipEquals(rec.getContent(), ip)) {
      rec.setContent(ip);
      cfDnsClient.recordUpdate(zone, rec);
      return true;
    }
    return false;
  }

  private boolean hasSubTld(CfDnsClient client, ZoneEntity zone, String host)
      throws CloudflareApiException {
    String sld = getSldFromHost(host);
    try {
      List<RecordEntity> recsA = client.recordList(zone, sld, RecordType.A, RecordType.AAAA);
      return !recsA.isEmpty();
    } catch (CloudflareApiException e) {
      log.error("Unexpected error while getting A record of host {}", host, e);
      throw e;
    }
  }
}
