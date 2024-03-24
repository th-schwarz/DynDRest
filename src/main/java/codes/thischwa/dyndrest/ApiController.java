package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.IpSetting;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The 'main' api controller. */
@RestController
@Slf4j
public class ApiController implements ApiRoutes {

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
  public ResponseEntity<Object> update(
      String host, String apitoken, InetAddress ipv4, InetAddress ipv6, HttpServletRequest req) {
    log.debug(
        "entered #update: host={}, apitoken={}, ipv4={}, ipv6={}", host, apitoken, ipv4, ipv6);
    validateHost(host, apitoken);

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
        updateLogService.log(host, reqIpSetting);
        return new ResponseEntity<>(HttpStatusCode.valueOf(config.updateIpChangedStatus()));
      }
    } catch (ProviderException e) {
      log.error("Updated host failed: " + host, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // building the update log
    updateLogService.log(host, reqIpSetting);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<IpSetting> info(String host, @RequestParam String apitoken) {
    log.debug("entered #info: host={}", host);
    // validation
    validateHost(host, apitoken);

    IpSetting ipSetting;
    try {
      ipSetting = provider.info(host);
    } catch (ProviderException e) {
      log.error("Zone info failed for: " + host, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return ResponseEntity.ok(ipSetting);
  }

  private void validateHost(String host, String apitoken) {
    try {
      boolean valid = hostZoneService.validate(host, apitoken);
      if (!valid) {
        log.warn("Validation: host {} not found.", host);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
    } catch (EmptyResultDataAccessException e) {
      log.warn("Validation: Unknown apitoken {} for host {}.", apitoken, host);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }
}
