package codes.thischwa.dyndrest.server;

import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.UpdateLogService;
import codes.thischwa.dyndrest.util.NetUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The 'main' api controller. */
@RestController
@Slf4j
public class ApiController implements ApiRoutes, RouterRoutes {

  private final Provider provider;

  private final AppConfig config;

  private final UpdateLogService updateLogService;

  private final HostZoneService hostZoneService;

  /**
   * Instantiates a new Api controller.
   *
   * @param provider the provider
   * @param config the app config
   * @param updateLogService the update log service
   * @param hostZoneService the service for maintaining hosts and zones
   */
  public ApiController(
      Provider provider,
      AppConfig config,
      UpdateLogService updateLogService,
      HostZoneService hostZoneService) {
    this.provider = provider;
    this.config = config;
    this.updateLogService = updateLogService;
    this.hostZoneService = hostZoneService;
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
    return updateIpAddresses(host, apiToken, ipv4, ipv6, req);
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

  @Override
  public ResponseEntity<Void> routerUpdateHost(
      String host, String apiToken, InetAddress ipv4, InetAddress ipv6, HttpServletRequest req) {
    log.debug(
        "entered #routerUpdateHost: host={}, apiToken={}, ipv4={}, ipv6={}",
        host,
        apiToken,
        ipv4,
        ipv6);
    return updateIpAddresses(host, apiToken, ipv4, ipv6, req);
  }

  private ResponseEntity<Void> updateIpAddresses(
      String host,
      String apiToken,
      @Nullable InetAddress ipv4,
      @Nullable InetAddress ipv6,
      HttpServletRequest req) {
    validateHost(host, apiToken);
    IpSetting reqIpSetting = new IpSetting(ipv4, ipv6);
    if (reqIpSetting.isNotSet()) {
      log.debug("Both IP parameters are null, try to fetch the remote IP.");
      String remoteIp = req.getRemoteAddr();
      if (remoteIp == null || !NetUtil.isIp(remoteIp)) {
        log.error("Couldn't determine the remote ip!");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
      log.debug("Fetched remote IP: {}", remoteIp);
      try {
        reqIpSetting = new IpSetting(remoteIp);
      } catch (UnknownHostException e) {
        log.error("Remote ip isn't valid: {}", remoteIp);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
    }

    // processing the update
    try {
      IpSetting current = provider.info(host);
      if (current.equals(reqIpSetting)) {
        log.debug("IPs didn't changed for {}, no update required!", host);
      } else {
        provider.processUpdate(host, reqIpSetting);
        log.info("Updated host {} successful with: {}", host, reqIpSetting);
        // building the update log
        updateLogService.log(host, reqIpSetting, UpdateLog.Status.success);
        return new ResponseEntity<>(HttpStatusCode.valueOf(config.updateIpChangedStatus()));
      }
    } catch (ProviderException e) {
      log.error("Updated host failed: " + host, e);
      updateLogService.log(host, reqIpSetting, UpdateLog.Status.failed);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return ResponseEntity.ok().build();
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
