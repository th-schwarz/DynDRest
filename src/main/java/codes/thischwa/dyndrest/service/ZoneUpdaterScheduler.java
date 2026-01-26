package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
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
  private final Provider provider;

  /**
   * Constructs a new ZoneUpdaterScheduler for scheduling and managing zone updates.
   *
   * @param zoneUpdaterService The service responsible for handling zone updates and managing related zone information.
   * @param provider The provider responsible for executing patch operations on zones.
   */
  public ZoneUpdaterScheduler(ZoneUpdaterService zoneUpdaterService, Provider provider) {
    this.zoneUpdaterService = zoneUpdaterService;
    this.provider = provider;
  }

  @Scheduled(fixedDelayString = "${dyndrest.zone-update-scheduler-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  void process() {
    log.debug("Starting zone addOrUpdate...");
    for (String zone : zoneUpdaterService.getZones()) {
      List<HostInfoHolder> hostsPerZoneToCreate = zoneUpdaterService.getHostsPerZoneToCreate().get(zone);
      List<HostInfoHolder> hostsPerZoneToUpdate = zoneUpdaterService.getHostsPerZoneToUpdate().get(zone);
      List<String> hostsPerZoneToDelete = zoneUpdaterService.getHostsPerZoneToDelete().get(zone);
      if (hostsPerZoneToCreate == null && hostsPerZoneToUpdate == null && hostsPerZoneToDelete == null) {
        log.debug("No changes found for zone: {}", zone);
        continue;
      }
      try {
        provider.patch(zone, hostsPerZoneToCreate, hostsPerZoneToUpdate, hostsPerZoneToDelete);
        if (hostsPerZoneToUpdate != null) {
          zoneUpdaterService.afterUpdate(hostsPerZoneToUpdate.toArray(new HostInfoHolder[0]));
        }
      } catch (ProviderException e) {
        log.error("Error while updating zone: {}", zone, e);
      }
    }
  }
}
