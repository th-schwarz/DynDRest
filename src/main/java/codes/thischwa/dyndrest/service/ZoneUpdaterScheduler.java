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
    List<HostInfoHolder> hostsToCreate = zoneUpdaterService.getHostsToCreate(zone);
    List<HostInfoHolder> hostsToUpdate = zoneUpdaterService.getHostsToUpdate(zone);
    List<String> hostsToDelete = zoneUpdaterService.getHostsToDelete(zone);
    Set<String> discardedHosts = new HashSet<>();
    hostsToCreate = filterExistingHosts(hostsToCreate, discardedHosts);
    hostsToUpdate = filterExistingHosts(hostsToUpdate, discardedHosts);

    if (hostsToCreate.isEmpty() && hostsToUpdate.isEmpty() && hostsToDelete.isEmpty()) {
      log.debug("No changes found for zone: {}", zone);
      return;
    }

    try {
      provider.patch(zone, emptyToNull(hostsToCreate), emptyToNull(hostsToUpdate), emptyToNull(hostsToDelete));
      List<HostInfoHolder> updatedHosts = new ArrayList<>(hostsToCreate);
      updatedHosts.addAll(hostsToUpdate);
      if (!updatedHosts.isEmpty()) {
        zoneUpdaterService.afterUpdate(updatedHosts.toArray(new HostInfoHolder[0]));
      }
      if (!hostsToDelete.isEmpty()) {
        zoneUpdaterService.afterDelete(zone, hostsToDelete);
      }
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
