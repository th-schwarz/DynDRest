package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.AbstractJdbcEntity;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.model.ZoneImport;
import codes.thischwa.dyndrest.repository.HostRepo;
import codes.thischwa.dyndrest.repository.ZoneRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Service for validating and maintaining hosts. */
@Service
@Slf4j
public class HostZoneService {
  @Nullable private final ZoneImport zoneImport;
  private final HostRepo hostRepo;
  private final ZoneRepo zoneRepo;

  /** Constructor of the service for validating and maintaining hosts. */
  public HostZoneService(HostRepo hostRepo, ZoneImport zoneImport, ZoneRepo zoneRepo) {
    this.hostRepo = hostRepo;
    this.zoneImport = zoneImport;
    this.zoneRepo = zoneRepo;
  }

  /**
   * Validates the API token for a given host.
   *
   * @param fullHost the full host name
   * @param apiToken the API token to validate
   * @return true if the API token is valid for the host, false otherwise
   * @throws EmptyResultDataAccessException if the host cannot be found
   */
  public boolean validate(String fullHost, String apiToken) throws EmptyResultDataAccessException {
    Host host = hostRepo.findByFullHost(fullHost);
    return host.getApiToken().equals(apiToken);
  }

  /**
   * Retrieves a list of configured hosts.
   *
   * @return the list of configured hosts
   */
  public List<FullHost> getConfiguredHosts() {
    return hostRepo.findAllExtended();
  }

  /**
   * Retrieves the list of configured zones.
   *
   * @return The list of Zone objects representing the configured zones.
   */
  public List<Zone> getConfiguredZones() {
    return zoneRepo.findAll();
  }

  /**
   * Checks if the specified full host exists in the list of configured hosts.
   *
   * @param fullHostStr the full host to check
   * @return true if the host exists in the list of configured hosts, false otherwise
   */
  public boolean hostExists(String fullHostStr) {
    return Optional.ofNullable(hostRepo.findByFullHost(fullHostStr)).isPresent();
  }

  /**
   * Retrieves the FullHost object for the given fullHost.
   *
   * @param fullHostStr the full host to retrieve
   * @return the FullHost object for the given fullHost or null if not exists.
   */
  @Nullable
  public FullHost getHost(String fullHostStr) {
    return hostRepo.findByFullHost(fullHostStr);
  }

  /**
   * Checks if a zone exists by the given zone string.
   *
   * @param zoneStr the string representing the zone name
   * @return true if the zone exists, false otherwise
   */
  public boolean zoneExists(String zoneStr) {
    return Optional.ofNullable(zoneRepo.findByName(zoneStr)).isPresent();
  }

  /**
   * Retrieves the Zone object for the specified zone name.
   *
   * @param zoneStr the name of the zone
   * @return the Zone object for the specified zone name or null if not exists.
   */
  @Nullable
  public Zone getZone(String zoneStr) {
    return zoneRepo.findByName(zoneStr);
  }

  /**
   * Imports zones an application start. Just new hosts and zones will be saved! Existing ones won't
   * be updated.
   */
  public void importOnStart() {
    if (zoneImport == null) {
      return;
    }
    List<FullHost> hostsToImport = zoneImport.getHosts();
    if (hostsToImport.isEmpty()) {
      log.info("No zones found for import.");
      return;
    }
    log.debug("{} hosts found in the configuration for import.", hostsToImport.size());

    List<FullHost> hostsToSave =
        hostsToImport.stream().filter(fullHost -> !hostExists(fullHost.getFullHost())).toList();
    log.info("{} hosts found for import.", hostsToSave.size());
    for (FullHost fullHost : hostsToSave) {
      Zone zone = getZone(fullHost.getZone());
      if (zone == null) {
        Zone tmoZone = new Zone();
        tmoZone.setName(fullHost.getZone());
        tmoZone.setNs(fullHost.getNs());
        saveOrUpdate(tmoZone);
        fullHost.setZoneId(tmoZone.getId());
      } else {
        fullHost.setZoneId(zone.getId());
      }
      saveOrUpdate(fullHost);
    }
  }

  /**
   * Saves or updates a Host entity.
   *
   * @param host the Host entity to be saved or updated
   */
  public void saveOrUpdate(Host host) {
    preSaveOrUpdate(host);
    // hostRepo.save(host);
    Host tmpHost =
        Host.getInstance(host.getName(), host.getApiToken(), host.getZoneId(), host.getChanged());
    hostRepo.save(tmpHost);
    host.setId(tmpHost.getId());
    host.setChanged(tmpHost.getChanged());
  }

  /**
   * Saves or updates a Zone entity in the repository.
   *
   * @param zone the Zone entity to be saved or updated
   */
  public void saveOrUpdate(Zone zone) {
    preSaveOrUpdate(zone);
    zoneRepo.save(zone);
  }

  private void preSaveOrUpdate(AbstractJdbcEntity entity) {
    entity.setChanged(LocalDateTime.now());
  }
}
