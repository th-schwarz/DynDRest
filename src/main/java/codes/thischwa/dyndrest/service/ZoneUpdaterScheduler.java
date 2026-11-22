package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * The ZoneUpdaterService class is responsible for updating zones on a schedule. It is a Spring
 * service that uses the scheduling feature of Spring Framework to run the addOrUpdate task at regular
 * intervals.
 */
@ConditionalOnProperty(name = "dyndrest.zone-update-scheduler-enabled", havingValue = "true")
@Service
@EnableScheduling
@Slf4j
public class ZoneUpdaterScheduler {

  private final ZoneUpdaterService zoneUpdaterService;
  private final HostZoneService hostZoneService;
  private final Provider provider;

  /**
   * Constructs a new ZoneUpdaterScheduler for scheduling and managing zone updates.
   *
   * @param zoneUpdaterService The service responsible for handling zone updates and managing related zone information.
   * @param hostZoneService    The service responsible for checking configured hosts.
   * @param provider           The provider responsible for executing patch operations on zones.
   */
  public ZoneUpdaterScheduler(ZoneUpdaterService zoneUpdaterService, HostZoneService hostZoneService,
                              Provider provider) {
    this.zoneUpdaterService = zoneUpdaterService;
    this.hostZoneService = hostZoneService;
    this.provider = provider;
  }

  @Scheduled(fixedDelayString = "${dyndrest.zone-update-scheduler-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  void process() {
    log.debug("Starting zone addOrUpdate...");
    for (String zone : zoneUpdaterService.getZones()) {
      zoneUpdaterService.runExclusive(() -> processZone(zone));
    }
  }

  private void processZone(String zone) {
    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsToUpdate(zone);
    Set<String> discardedHosts = new HashSet<>();
    hostsToUpdate = filterExistingHosts(hostsToUpdate, discardedHosts);

    if (hostsToUpdate.isEmpty()) {
      log.debug("No changes found for zone: {}", zone);
      return;
    }

    try {
      provider.patch(zone, null, emptyToNull(hostsToUpdate), null);
      zoneUpdaterService.afterUpdate(hostsToUpdate.toArray(new HostInfoHolder[0]));
    } catch (ProviderException e) {
      log.error("Error while updating zone: {}", zone, e);
    }
  }

  private List<HostInfoHolder> filterExistingHosts(List<HostInfoHolder> hosts, Set<String> discardedHosts) {
    List<HostInfoHolder> existingHosts = new ArrayList<>();
    for (HostInfoHolder host : hosts) {
      if (hostZoneService.hostExists(host.getFullHost())) {
        existingHosts.add(host);
      } else if (discardedHosts.add(host.getFullHost())) {
        zoneUpdaterService.discardHost(host);
      }
    }
    return existingHosts;
  }

  private <T> @Nullable List<T> emptyToNull(List<T> values) {
    return values.isEmpty() ? null : values;
  }
}
