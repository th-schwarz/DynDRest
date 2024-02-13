package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.ZoneImport;
import codes.thischwa.dyndrest.repository.HostRepo;
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

  public HostZoneService(HostRepo hostRepo, ZoneImport zoneImport) {
    this.hostRepo = hostRepo;
    this.zoneImport = zoneImport;
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
   * Checks if the specified full host exists in the list of configured hosts.
   *
   * @param fullHost the full host to check
   * @return true if the host exists in the list of configured hosts, false otherwise
   */
  public boolean hostExists(String fullHost) {
    try {
      return Optional.ofNullable(hostRepo.findByFullHost(fullHost)).isPresent();
    } catch (EmptyResultDataAccessException e) {
      return false;
    }
  }

  /**
   * Retrieves the FullHost object for the given fullHost.
   *
   * @param fullHost the full host to retrieve
   * @return the FullHost object for the given fullHost
   * @throws EmptyResultDataAccessException if no FullHost object is found for the given fullHost
   */
  public FullHost getHost(String fullHost) throws EmptyResultDataAccessException {
    return hostRepo.findByFullHost(fullHost);
  }

  public void importOnStart() {
    if (zoneImport == null) {
      return;
    }
    List<FullHost> hostsToImport = zoneImport.getHosts();
    if (hostsToImport.isEmpty()) {
      log.info("No zones found for import.");
      return;
    }
    log.info("{} zones found for import.", hostsToImport.size());
  }
}
