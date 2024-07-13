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
  private final ZoneImport zoneImport;
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
    Optional<FullHost> optHost = hostRepo.findByFullHost(fullHost);
    if (optHost.isEmpty()) {
      throw new EmptyResultDataAccessException("Host not found: " + fullHost, 1);
    }
    return optHost.get().getApiToken().equals(apiToken);
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
    return hostRepo.findByFullHost(fullHostStr).isPresent();
  }

  /**
   * Retrieves the FullHost object for the given fullHost.
   *
   * @param fullHostStr the full host to retrieve
   * @return the FullHost object for the given fullHost or null if not exists.
   */
  public Optional<FullHost> getHost(String fullHostStr) {
    return hostRepo.findByFullHost(fullHostStr);
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
    List<FullHost> hostsToImport = zoneImport.getHosts();
    if (hostsToImport.isEmpty()) {
      log.info("No zones found for import.");
      return;
    }
    log.debug("{} hosts found in the configuration for import.", hostsToImport.size());

    List<FullHost> hostsToSave =
        hostsToImport.stream().filter(fullHost -> !hostExists(fullHost.getFullHost())).toList();
    for (FullHost fullHost : hostsToSave) {
      Zone zone = getZone(fullHost.getZone());
      if (zone == null) {
        Zone tmpZone = new Zone();
        tmpZone.setName(fullHost.getZone());
        tmpZone.setNs(fullHost.getNs());
        saveOrUpdate(tmpZone);
        fullHost.setZoneId(tmpZone.getId());
      } else {
        fullHost.setZoneId(zone.getId());
      }
      saveOrUpdate(fullHost);
    }
    if (!hostsToSave.isEmpty()) {
      log.info("{} hosts successful imported.", hostsToSave.size());
    }
  }

  /**
   * Saves or updates a Host entity.
   *
   * @param host the Host entity to be saved or updated
   */
  public void saveOrUpdate(Host host) {
    preSaveOrUpdate(host);
    Host tmpHost = Host.getInstance(host);
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

  /**
   * Retrieves a host by its ID.
   *
   * @param id The ID of the host to retrieve.
   * @return An Optional object that contains the host if found, or an empty Optional if not found.
   */
  public Optional<Host> findHostById(Integer id) {
    return hostRepo.findById(id);
  }

  /**
   * Finds all hosts of a specified zone.
   *
   * @param zoneName the name of the zone to find hosts for
   * @return a list of FullHost objects representing the hosts in the specified zone
   */
  public Optional<List<FullHost>> findHostsOfZone(String zoneName) {
    Zone zone = zoneRepo.findByName(zoneName);
    if (zone == null) {
      log.warn("Zone isn't configured: " + zoneName);
      return Optional.empty();
    }
    return Optional.of(hostRepo.findByZoneId(zone.getId()));
  }

  /**
   * Deletes a zone from the repository.
   *
   * @param zone the zone to be deleted
   */
  public void deleteZone(Zone zone) {
    zoneRepo.delete(zone);
  }
}
