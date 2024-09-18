package codes.thischwa.dyndrest.server;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.util.DomainNameValidator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

/** The AdminController provides methods for managing zones and hosts in the system. */
@Controller
@Slf4j
public class AdminController implements AdminRoutes {

  private final DomainNameValidator domainNameValidator = new DomainNameValidator();
  private final AppConfig config;
  private final HostZoneService hostZoneService;
  private final Provider provider;

  /**
   * Creates a new instance of the AdminController class.
   *
   * @param config The application configuration.
   * @param hostZoneService The host zone service.
   * @param provider The provider.
   */
  public AdminController(AppConfig config, HostZoneService hostZoneService, Provider provider) {
    this.config = config;
    this.hostZoneService = hostZoneService;
    this.provider = provider;
  }

  @Override
  public ResponseEntity<Void> addZone(String zoneName, String ns, String adminToken) {
    log.debug("entered #addZone: name={}, ns={}", zoneName, ns);
    assert config.adminApiToken() != null;
    if (!config.adminApiToken().equals(adminToken)) {
      log.error("Invalid admin token.");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    if (!domainNameValidator.isValidDomainNames(zoneName, ns)) {
      log.error("Domain name ({}) or nameserver ({}) is malformed.", zoneName, ns);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    try {
      hostZoneService.addZone(zoneName, ns);
    } catch (DbActionExecutionException e) {
      if (e.getCause() instanceof DuplicateKeyException) {
        log.error("Zone with name {} already exists.", zoneName);
        throw new ResponseStatusException(HttpStatus.CONFLICT);
      } else {
        log.error("Failed to add zone: name={}, ns={}", zoneName, ns, e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deleteZone(String name, String adminToken) {
    log.debug("entered #deleteZone: name={}", name);
    checkAdminToken(adminToken);
    Zone zone = hostZoneService.getZone(name);
    if (zone == null) {
      log.error("Zone with name {} not found.", name);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    hostZoneService.deleteZone(zone);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<List<Zone>> listZones(String adminToken) {
    log.debug("entered #listZones.");
    checkAdminToken(adminToken);
    List<Zone> zones = hostZoneService.getAllZones();
    return ResponseEntity.ok(zones);
  }

  @Override
  public ResponseEntity<List<HostEnriched>> listHostsOfZone(String zoneName, String adminToken) {
    log.debug("entered #listHostsOfZone.");
    checkAdminToken(adminToken);
    Optional<List<HostEnriched>> hosts = hostZoneService.findHostsOfZone(zoneName);
    return hosts.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<Void> addHost(
      String zoneName, String host, String apiToken, String adminToken) {
    log.debug("entered #addHost: name={}, host={}", zoneName, host);
    checkAdminToken(adminToken);
    Zone zone = hostZoneService.getZone(zoneName);
    if (zone == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    Optional<List<HostEnriched>> fullHosts = hostZoneService.findHostsOfZone(zoneName);
    if (fullHosts.isPresent() && fullHosts.get().stream().anyMatch(h -> h.getName().equals(host))) {
      log.error("Host {} already exists.", host);
      throw new ResponseStatusException(HttpStatus.CONFLICT);
    }
    try {
      provider.addHost(zoneName, host);
    } catch (ProviderException e) {
      log.error("Failed to add host: zone=" + zoneName + ", host=" + host, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    hostZoneService.addHost(zone, host, apiToken);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deleteHost(String host, String adminToken) {
    log.debug("entered #deleteHost: fullHost={}", host);
    checkAdminToken(adminToken);
    Optional<HostEnriched> optionalFullHost = hostZoneService.getHost(host);
    if (optionalFullHost.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    HostEnriched hostEnriched = optionalFullHost.get();
    try {
      provider.removeHost(hostEnriched.getFullHost());
    } catch (ProviderException e) {
      log.error("Failed to remove host: fullHost=" + hostEnriched.getFullHost(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    hostZoneService.deleteHost(hostEnriched);
    return ResponseEntity.ok().build();
  }

  private void checkAdminToken(String adminToken) {
    assert config.adminApiToken() != null;
    if (!config.adminApiToken().equals(adminToken)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin token");
    }
  }
}
