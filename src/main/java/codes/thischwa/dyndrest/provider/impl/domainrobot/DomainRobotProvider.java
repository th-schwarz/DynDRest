package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.domainrobot.sdk.models.generated.Zone;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;

/**
 * Provider implementation for the domainrobot sdk.
 */
@Slf4j
class DomainRobotProvider extends GenericProvider implements InitializingBean {

  @Getter
  private final ZoneClientWrapper zcw;

  /**
   * Instantiates a new Domain robot provider.
   *
   * @param appConfig            the app config
   * @param hostZoneService      the host zone service
   * @param zcw                  the zcw
   * @param securityChainManager the dynamic security chain manager
   */
  DomainRobotProvider(AppConfig appConfig, HostZoneService hostZoneService, ZoneUpdaterService zoneUpdaterService,
                      ZoneClientWrapper zcw,
                      DynamicSecurityChainManager securityChainManager) {
    super(appConfig, securityChainManager, hostZoneService, zoneUpdaterService);
    this.zcw = zcw;
  }

  @Override
  public void addOrUpdate(String fqdn, IpSetting ipSetting) throws ProviderException {
    String sld = fqdn.substring(0, fqdn.indexOf("."));

    // set the IPs in the zone object
    Zone zone = fetchZoneFromHost(fqdn);
    if (!zcw.hasIpsChanged(zone, sld, ipSetting)) {
      return;
    }
    zcw.process(zone, sld, ipSetting);

    // processing the addOrUpdate
    zcw.update(zone);
  }

  @Override
  public IpSetting info(String fqdn) throws ProviderException {
    Zone zone = fetchZoneFromHost(fqdn);
    return zcw.info(zone, fqdn.substring(0, fqdn.indexOf(".")));
  }

  @Override
  public void removeHostIpSettings(String fqdn) throws ProviderException {
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(fqdn);
    if (optFullHost.isEmpty()) {
      throw new ProviderException("Host isn't configured: " + fqdn);
    }
    HostEnriched hostEnriched = optFullHost.get();
    Zone zone = fetchZoneFromHost(fqdn);
    zcw.removeSld(zone, hostEnriched.getSld());
    zcw.update(zone);
  }

  @Override
  public void patch(String zoneStr, @Nullable List<HostInfoHolder> creates, @Nullable List<HostInfoHolder> updates,
                    @Nullable List<String> deletes) throws ProviderException {
    codes.thischwa.dyndrest.model.Zone zoneDb = hostZoneService.getZone(zoneStr);
    if (zoneDb == null) {
      throw new IllegalArgumentException("Zone not found: " + zoneStr);
    }
    Zone zone = zcw.info(zoneDb.getName(), zoneDb.getNs());
    List<HostInfoHolder> hostsToProcess = new ArrayList<>();

    if (creates != null) {
      hostsToProcess.addAll(creates);
    }
    if (updates != null) {
      hostsToProcess.addAll(updates);
    }
    if (!hostsToProcess.isEmpty()) {
      for (HostInfoHolder create : hostsToProcess) {
        zcw.process(zone, create.getSld(), create.getIpSetting());
      }
    }
    if (deletes != null) {
      for (String delete : deletes) {
        zcw.removeSld(zone, delete);
      }
    }
    zcw.update(zone);
  }

  @Override
  public void confirmZone(codes.thischwa.dyndrest.model.Zone myZone)
      throws IllegalArgumentException {
    Zone zone;
    try {
      zone = zcw.info(myZone.getName(), myZone.getNs());
      log.info("*** Zone confirmed: {}", zone.getOrigin());
    } catch (ProviderException e) {
      log.error("Error while getting zone info of {}", myZone.getName(), e);
      throw new IllegalArgumentException("Zone couldn't be confirmed.");
    }
    hostsOfZoneConfirmed(zone);
  }

  @Override
  public List<HostInfoHolder> getCurrentConfiguredHosts(List<HostEnriched> hostsEnriched) throws ProviderException {
    Map<String, Zone> knownRealZones = new HashMap<>();
    List<HostInfoHolder> hosts = new ArrayList<>();

    for (HostEnriched host : hostsEnriched) {
      if (!knownRealZones.containsKey(host.getZone())) {
        Zone zone = zcw.info(host.getZone(), host.getNs());
        knownRealZones.put(host.getZone(), zone);
      }

      Zone zone = knownRealZones.get(host.getZone());
      HostInfoHolder hostInfoHolder = HostInfoHolder.of(host, zcw.info(zone, host.getSld()));
      hosts.add(hostInfoHolder);
    }

    return hosts;
  }

  private void hostsOfZoneConfirmed(Zone zone) throws IllegalArgumentException {
    Optional<List<HostEnriched>> opt = hostZoneService.findHostsOfZone(zone.getOrigin());
    if (opt.isEmpty()) {
      log.warn("No hosts found for zone: {}", zone.getOrigin());
      return;
    }
    for (HostEnriched host : opt.get()) {
      if (zcw.hasSubTld(zone, host.getSld())) {
        log.info("Host confirmed: {}", host.getFullHost());
      } else {
        throw new IllegalArgumentException("Host not confirmed: " + host.getFullHost());
      }
    }
  }

  /**
   * Zone info zone.
   *
   * @param fqdn the host
   * @return the zone
   * @throws ProviderException        the provider exception
   * @throws IllegalArgumentException the illegal argument exception
   */
  Zone fetchZoneFromHost(String fqdn) throws ProviderException, IllegalArgumentException {
    Optional<HostEnriched> optFullHost = hostZoneService.getHost(fqdn);
    if (optFullHost.isEmpty()) {
      throw new IllegalArgumentException("Host isn't configured: " + fqdn);
    }
    HostEnriched hostEnriched = optFullHost.get();
    String zone = hostEnriched.getZone();
    String primaryNameServer = hostEnriched.getNs();
    return zcw.info(zone, primaryNameServer);
  }

  @Override
  public void afterPropertiesSet() throws IllegalArgumentException {
    validateHostZoneConfiguration();
  }
}
