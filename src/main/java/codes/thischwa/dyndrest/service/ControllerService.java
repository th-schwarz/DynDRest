package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.util.NetUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class that handles IP update processing for a given host.
 *
 * The {@code ControllerService} is responsible for orchestrating the validation and update of IP
 * settings associated with a host. It integrates with a DNS provider implementation, application
 * configuration, and a logging service to ensure that updates are processed correctly and recorded
 * accordingly.
 */
@Slf4j
@Service
public class ControllerService {

  private final Provider provider;

  private final AppConfig config;

  private final UpdateLogService updateLogService;

  public ControllerService(Provider provider, AppConfig config, UpdateLogService updateLogService) {
    this.provider = provider;
    this.config = config;
    this.updateLogService = updateLogService;
  }

  public ResponseEntity<Void> processIpUpdate(String host, @Nullable InetAddress ipv4,
                                              @Nullable InetAddress ipv6, HttpServletRequest req) {
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
}
