package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import org.domainrobot.sdk.client.clients.ZoneClient;
import org.domainrobot.sdk.models.DomainrobotApiException;
import org.domainrobot.sdk.models.generated.ResourceRecord;
import org.domainrobot.sdk.models.generated.Zone;
import org.jspecify.annotations.Nullable;

/** Encapsulate the {@link ZoneClient} and adds same useful util methods. */
class ZoneClientWrapper {

  enum ResourceRecordTypeIp {
    A,
    AAAA
  }

  private final Map<String, String> customHeaders;
  private final long defaultTtl;
  private final ZoneClient zc;

  ZoneClientWrapper(ZoneClient zc, Map<String, String> customHeaders, long defaultTtl) {
    this.zc = zc;
    this.customHeaders = customHeaders;
    this.defaultTtl = defaultTtl;
  }

  boolean hasSubTld(Zone zone, String sld) {
    return zone.getResourceRecords().stream()
        .anyMatch(
            rr ->
                (rr.getType().equals(ResourceRecordTypeIp.A.toString())
                        || rr.getType().equals(ResourceRecordTypeIp.AAAA.toString()))
                    && rr.getName().equals(sld));
  }

  @Nullable
  ResourceRecord searchResourceRecord(Zone zone, String name, ResourceRecordTypeIp type) {
    return zone.getResourceRecords().stream()
        .filter(rr -> rr.getType().equals(type.toString()) && rr.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  String deriveZone(String host) {
    long cnt = host.chars().filter(ch -> ch == '.').count();
    if (cnt < 2) {
      throw new IllegalArgumentException("'host' must be a sub domain.");
    }
    return host.substring(host.indexOf(".") + 1);
  }

  boolean hasIpsChanged(Zone zone, String sld, IpSetting ipSetting) {
    if (ipSetting.isNotSet()) {
      return false;
    }
    ResourceRecord rrv4 = searchResourceRecord(zone, sld, ResourceRecordTypeIp.A);
    ResourceRecord rrv6 = searchResourceRecord(zone, sld, ResourceRecordTypeIp.AAAA);
    boolean ipv4Changed = !hasIpChanged(rrv4, ipSetting.getIpv4());
    boolean ipv6Changed = !hasIpChanged(rrv6, ipSetting.getIpv6());
    return ipv4Changed || ipv6Changed;
  }

  private boolean hasIpChanged(@Nullable ResourceRecord rr, @Nullable InetAddress ip) {
    if (rr == null || ip == null) {
      return false;
    }
    try {
      InetAddress rrIp = InetAddress.getByName(rr.getValue());
      return rrIp.equals(ip);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Couldn't get Ip from value: " + rr.getValue(), e);
    }
  }

  void update(Zone zone) throws ProviderException {
    try {
      zc.update(zone, customHeaders);
    } catch (DomainrobotApiException e) {
      throw new ProviderException("API exception", e);
    } catch (Exception e) {
      throw new ProviderException("Unknown exception", e);
    }
  }

  /**
   * Processes a zone-info for 'zone' and 'primaryNameServer'.
   *
   * @param zone the zone to process the info
   * @param primaryNameServer the primary NS of the zone
   * @return the complete zone object from the domainrobot sdk
   * @throws ProviderException if an exception happens while processing the zone-info
   */
  Zone info(String zone, String primaryNameServer) throws ProviderException {
    try {
      return zc.info(zone, primaryNameServer, customHeaders);
    } catch (DomainrobotApiException e) {
      throw new ProviderException("Domain-Robot-API exception", e);
    } catch (Exception e) {
      throw new ProviderException("Unexpected exception", e);
    }
  }

  /**
   * Processes the ip settings for the desired zone and subtld, The corresponding resource record
   * will be updated or removed if null.
   *
   * @param zone the zone
   * @param sld the sld
   * @param ipSetting the ip setting
   */
  void process(Zone zone, String sld, IpSetting ipSetting) {
    processIpv4(zone, sld, ipSetting.getIpv4());
    processIpv6(zone, sld, ipSetting.getIpv6());
  }

  /**
   * Retrieves the IP settings for the given zone and second-level domain (SLD).
   * The method searches for resource records of type A (IPv4) and AAAA (IPv6) within the zone
   * and updates the corresponding IP settings.
   *
   * @param zone the zone in which to search for resource records
   * @param sld the second-level domain to look up IP settings for
   * @return an {@link IpSetting} object containing the IPv4 and/or IPv6 address, or an empty
   *         {@code IpSetting} if no matching resource records are found
   */
  IpSetting info(Zone zone, String sld) {
    IpSetting ipSetting = new IpSetting();
    ResourceRecord rrv4 = searchResourceRecord(zone, sld, ResourceRecordTypeIp.A);
    ResourceRecord rrv6 = searchResourceRecord(zone, sld, ResourceRecordTypeIp.AAAA);
    if (rrv4 != null) {
      ipSetting.setIpv4(rrv4.getValue());
    }
    if (rrv6 != null) {
      ipSetting.setIpv6(rrv6.getValue());
    }
    return ipSetting;
  }

  private void processIpv4(Zone zone, String sld, @Nullable Inet4Address ip) {
    if (ip != null) {
      addOrUpdateIpv4(zone, sld, ip);
    } else {
      removeIpv4(zone, sld);
    }
  }

  private void processIpv6(Zone zone, String sld, @Nullable Inet6Address ip) {
    if (ip != null) {
      addOrUpdateIpv6(zone, sld, ip);
    } else {
      removeIp6(zone, sld);
    }
  }

  private void addOrUpdateIpv4(Zone zone, String sld, Inet4Address ip) {
    addOrUpdateIp(zone, sld, ip, ResourceRecordTypeIp.A);
  }

  private void addOrUpdateIpv6(Zone zone, String sld, Inet6Address ip) {
    addOrUpdateIp(zone, sld, ip, ResourceRecordTypeIp.AAAA);
  }

  private void addOrUpdateIp(Zone zone, String sld, InetAddress ip, ResourceRecordTypeIp type) {
    ResourceRecord rr = searchResourceRecord(zone, sld, type);
    if (rr != null) {
      rr.setValue(ip.getHostAddress());
      rr.setTtl(defaultTtl);
    } else {
      ResourceRecord rrSld = new ResourceRecord();
      rrSld.setName(sld);
      rrSld.setValue(ip.getHostAddress());
      rrSld.setType(type.toString());
      rrSld.setTtl(defaultTtl);
      zone.getResourceRecords().add(rrSld);
    }
  }

  void removeSld(Zone zone, String sld) {
    removeIpv4(zone, sld);
    removeIp6(zone, sld);
  }

  void removeIpv4(Zone zone, String sld) {
    removeIp(zone, sld, ResourceRecordTypeIp.A);
  }

  void removeIp6(Zone zone, String sld) {
    removeIp(zone, sld, ResourceRecordTypeIp.AAAA);
  }

  void removeIp(Zone zone, String sld, ResourceRecordTypeIp type) {
    ResourceRecord rr = searchResourceRecord(zone, sld, type);
    if (rr != null) {
      zone.getResourceRecords().remove(rr);
    }
  }

}
