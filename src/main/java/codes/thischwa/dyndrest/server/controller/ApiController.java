package codes.thischwa.dyndrest.server.controller;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.service.ControllerService;
import codes.thischwa.dyndrest.service.HostZoneService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The 'main' api controller.
 */
@RestController
@Slf4j
public class ApiController implements ApiRoutes {

  private final Provider provider;

  private final HostZoneService hostZoneService;

  private final ControllerService controllerService;


  /**
   * Instantiates a new Api controller.
   *
   * @param provider             the provider
   * @param hostZoneService      the service for maintaining hosts and zones
   */
  public ApiController(
      Provider provider,
      HostZoneService hostZoneService, ControllerService controllerService) {
    this.provider = provider;
    this.hostZoneService = hostZoneService;
    this.controllerService = controllerService;
  }

  @Override
  public ResponseEntity<Void> updateHost(
      String host,
      String apiToken,
      @Nullable InetAddress ipv4,
      @Nullable InetAddress ipv6,
      HttpServletRequest req) {
    log.debug(
        "entered #update: host={}, apiToken={}, ipv4={}, ipv6={}", host, apiToken, ipv4, ipv6);
    validateHost(host, apiToken);
    return controllerService.processIpUpdate(host, ipv4, ipv6, req);
  }

  @Override
  public ResponseEntity<IpSetting> fetchHostIpSetting(String host, @RequestParam String apiToken) {
    log.debug("entered #info: host={}", host);
    // validation
    validateHost(host, apiToken);

    IpSetting ipSetting;
    try {
      ipSetting = provider.info(host);
    } catch (ProviderException e) {
      log.error("Zone info failed for: " + host, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return ResponseEntity.ok(ipSetting);
  }

  private void validateHost(String host, String apiToken) {
    try {
      boolean valid = hostZoneService.validate(host, apiToken);
      if (!valid) {
        log.warn("Validation: apiToken isn't valid for host {}.", host);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      }
    } catch (EmptyResultDataAccessException e) {
      log.warn("Validation: Host {} not found.", host);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }
}
