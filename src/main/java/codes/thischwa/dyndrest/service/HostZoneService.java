package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.AbstractJdbcEntity;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.model.config.ZoneImportConfig;
import codes.thischwa.dyndrest.repository.HostRepo;
import codes.thischwa.dyndrest.repository.ZoneRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Service for validating and maintaining hosts. */
@Service
@Slf4j
public class HostZoneService {
  private final ZoneImportConfig zoneImportConfig;
  private final HostRepo hostRepo;
  private final ZoneRepo zoneRepo;

  /** Constructor of the service for validating and maintaining hosts. */
  public HostZoneService(HostRepo hostRepo, ZoneImportConfig zoneImportConfig, ZoneRepo zoneRepo) {
    this.hostRepo = hostRepo;
    this.zoneImportConfig = zoneImportConfig;
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
    Optional<HostEnriched> optHost = hostRepo.findByFullHost(fullHost);
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
  public List<HostEnriched> getConfiguredHosts() {
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
  public Optional<HostEnriched> getHost(String fullHostStr) {
    return hostRepo.findByFullHost(fullHostStr);
  }

  /**
   * Retrieves the Zone object for the specified zone name.
   *
   * @param name the name of the zone
   * @return the Zone object for the specified zone name or null if not exists.
   */
  @Nullable
  public Zone getZone(String name) {
    return zoneRepo.findByName(name);
  }

  /**
   * Imports zones an application start. Just new hosts and zones will be saved! Existing ones won't
   * be updated.
   *
   * <p>It is called from {@link
   * codes.thischwa.dyndrest.server.config.ApplicationStartup#onApplicationEvent(ApplicationReadyEvent)}
   */
  public void importOnStart() {
    List<HostEnriched> hostsToImport = zoneImportConfig.getHosts();
    if (hostsToImport.isEmpty()) {
      log.info("No zones found for import.");
      return;
    }

    List<HostEnriched> hostsToSave =
        hostsToImport.stream().filter(fullHost -> !hostExists(fullHost.getFullHost())).toList();
    for (HostEnriched hostEnriched : hostsToSave) {
      Zone zone = getZone(hostEnriched.getZone());
      if (zone == null) {
        Zone tmpZone = new Zone();
        tmpZone.setName(hostEnriched.getZone());
        tmpZone.setNs(hostEnriched.getNs());
        saveOrUpdate(tmpZone);
        log.debug("Zone imported successfully: {}", tmpZone.getName());
        hostEnriched.setZoneId(tmpZone.getId());
      } else {
        hostEnriched.setZoneId(zone.getId());
      }
      saveOrUpdate(hostEnriched);
      log.debug("Host imported successfully: {}", hostEnriched.getFullHost());
    }
    log.debug(
        "{} hosts found in the configuration for import and {} host were imported successfully.",
        hostsToImport.size(),
        hostsToSave.size());
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

  /**
   * Adds a new Zone with the specified name and name server to the system.
   *
   * @param name The name of the Zone.
   * @param ns The name server of the Zone.
   * @return The newly created Zone.
   */
  public Zone addZone(String name, String ns) {
    Zone z = new Zone();
    z.setName(name);
    z.setNs(ns);
    saveOrUpdate(z);
    return z;
  }

  private void preSaveOrUpdate(AbstractJdbcEntity entity) {
    entity.setChanged(LocalDateTime.now());
  }

  /**
   * Adds a new host to the specified zone.
   *
   * @param zone the zone for the host
   * @param hostname the hostname of the host
   * @param apiToken the API token for the host
   * @return the newly created host
   */
  public Host addHost(Zone zone, String hostname, String apiToken) {
    Host host = new Host();
    host.setZoneId(zone.getId());
    host.setName(hostname);
    host.setApiToken(apiToken);
    saveOrUpdate(host);
    return host;
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
  public Optional<List<HostEnriched>> findHostsOfZone(String zoneName) {
    Zone zone = zoneRepo.findByName(zoneName);
    if (zone == null) {
      log.warn("Zone isn't configured: " + zoneName);
      return Optional.empty();
    }
    if (zone.getId() == null) {
      throw new IllegalArgumentException("Zone id shouldn't be null.");
    }
    return Optional.of(hostRepo.findByZoneId(zone.getId()));
  }

  /**
   * Deletes a zone.
   *
   * @param zone the zone to be deleted
   */
  public void deleteZone(Zone zone) {
    zoneRepo.delete(zone);
  }

  /**
   * Retrieves all zones.
   *
   * @return a list of all zones in the system.
   */
  public List<Zone> getAllZones() {
    return zoneRepo.findAll();
  }

  /**
   * Deletes a host from the system.
   *
   * @param host the Host object to be deleted
   */
  public void deleteHost(Host host) {
    if (host.getId() == null) {
      throw new IllegalArgumentException("Host id should not be null.");
    }
    hostRepo.deleteById(host.getId());
  }
}
