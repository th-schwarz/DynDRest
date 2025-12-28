package codes.thischwa.dyndrest.server.controller;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.ControllerService;
import codes.thischwa.dyndrest.service.HostZoneService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling router-specific routes for updating host IP addresses.
 * This controller provides an alternative mechanism for routers that do not support the PUT method.
 */
@RestController
@Slf4j
public class RouterController implements RouterRoutes {

  private final ControllerService controllerService;

  private final HostZoneService hostZoneService;

  private final DynamicSecurityChainManager securityChainManager;

  public RouterController(ControllerService controllerService, HostZoneService hostZoneService,
                          DynamicSecurityChainManager securityChainManager) {
    this.controllerService = controllerService;
    this.hostZoneService = hostZoneService;
    this.securityChainManager = securityChainManager;
  }

  @Override
  public ResponseEntity<Void> routerUpdateHost(@AuthenticationPrincipal UserDetails userDetails, String host, InetAddress ipv4,
                                               InetAddress ipv6, HttpServletRequest req) {
    log.debug(
        "entered #routerUpdateHost: host={}, ipv4={}, ipv6={}",
        host,
        ipv4,
        ipv6);
    // Check if the host exists
    Optional<HostEnriched> optHost = hostZoneService.getHost(host);
    if (optHost.isEmpty()) {
      log.warn("Host not found: {}", host);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // CRITICAL: Check if the authenticated username matches the host in the path
    String authenticatedUsername = userDetails.getUsername();
    if (!authenticatedUsername.equals(host) || !securityChainManager.isHostRegistered(host)) {
      log.warn("Authorization failed: User {} attempted to update host {}", authenticatedUsername, host);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    log.debug("Authorization successful: User {} updating host {}", authenticatedUsername, host);
    ResponseEntity<Void> response = controllerService.processIpUpdate(host, ipv4, ipv6, req);
    return response;
  }
}
