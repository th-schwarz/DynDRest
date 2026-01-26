package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.util.NetUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class that handles IP addOrUpdate processing for a given host.
 *
 * <p>The {@code ControllerService} is responsible for orchestrating the validation and addOrUpdate of IP
 * settings associated with a host. It integrates with a DNS provider implementation, application
 * configuration, and a logging service to ensure that updates are processed correctly and recorded
 * accordingly.
 */
@Slf4j
@Service
public class ControllerService {

  private final UpdateLogService updateLogService;

  private final ZoneUpdaterService zoneUpdaterService;

  private final HostZoneService hostZoneService;

  private final AppConfig appConfig;
  private final Provider provider;

  /**
   * Constructor for ControllerService.
   *
   * @param updateLogService   the update log service
   * @param zoneUpdaterService the zone updater service
   * @param hostZoneService    the host zone service
   * @param appConfig          the application configuration
   * @param provider           the DNS provider
   */
  public ControllerService(UpdateLogService updateLogService,
                           ZoneUpdaterService zoneUpdaterService, HostZoneService hostZoneService, AppConfig appConfig,
                           Provider provider) {
    this.updateLogService = updateLogService;
    this.zoneUpdaterService = zoneUpdaterService;
    this.hostZoneService = hostZoneService;
    this.appConfig = appConfig;
    this.provider = provider;
  }

  /**
   * Processes an IP update request for a specific host.
   *
   * @param host the full host name
   * @param ipv4 the IPv4 address (optional)
   * @param ipv6 the IPv6 address (optional)
   * @param req  the HTTP servlet request
   * @return a response entity indicating success or failure
   */
  public ResponseEntity<Void> processIpUpdate(String host, @Nullable InetAddress ipv4,
                                              @Nullable InetAddress ipv6, HttpServletRequest req) {
    Optional<HostEnriched> optHost = hostZoneService.getHost(host);
    if (optHost.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    IpSetting reqIpSetting = new IpSetting(ipv4, ipv6);
    if (reqIpSetting.isNotSet()) {
      log.debug("Both IP parameters are null, try to fetch the remote IP.");
      InetAddress remoteIp;
      try {
        remoteIp = NetUtil.fetchRemoteAddress(req);
      } catch (Exception e) {
        log.error("Couldn't determine the remote ip!");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
      log.debug("Fetched remote IP: {}", remoteIp.getHostAddress());
      try {
        reqIpSetting = new IpSetting(remoteIp);
      } catch (IllegalArgumentException e) {
        log.error("Remote ip isn't valid: {}", remoteIp.getHostAddress());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
    }

    // add host order
    HostInfoHolder hostInfoHolder = HostInfoHolder.of(optHost.get(), reqIpSetting);
    processIpUpdate(hostInfoHolder);
    return ResponseEntity.ok().build();
  }

  /**
   * Processes an IP update for a host information holder.
   *
   * @param hostInfoHolder the host information holder containing the update details
   */
  public void processIpUpdate(HostInfoHolder hostInfoHolder) {
    if (appConfig.zoneUpdateSchedulerEnabled()) {
      updateLogService.log(hostInfoHolder.getFullHost(), hostInfoHolder.getIpSetting(),
          UpdateLog.Status.waiting);
      zoneUpdaterService.addOrUpdateHost(hostInfoHolder);
      return;
    }

    try {
      provider.addOrUpdate(hostInfoHolder.getFullHost(), hostInfoHolder.getIpSetting());
    } catch (ProviderException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
