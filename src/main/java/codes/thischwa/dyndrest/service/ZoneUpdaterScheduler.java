package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * The ZoneUpdaterService class is responsible for updating zones on a schedule. It is a Spring
 * service that uses the scheduling feature of Spring Framework to run the update task at regular
 * intervals.
 */
@Service
@EnableScheduling
@Slf4j
public class ZoneUpdaterScheduler {

  private final ZoneUpdateOrderService zoneUpdateOrderService;
  private final Provider provider;

  public ZoneUpdaterScheduler(ZoneUpdateOrderService zoneUpdateOrderService, Provider provider) {
    this.zoneUpdateOrderService = zoneUpdateOrderService;
    this.provider = provider;
  }

  @Scheduled(fixedDelayString = "${dyndrest.update-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  void process() {
    log.debug("Starting zone update...");
    for (String zone : zoneUpdateOrderService.getZones()) {
      List<HostInfoHolder> hostsPerZoneToCreate = zoneUpdateOrderService.getHostsPerZoneToCreate().get(zone);
      List<HostInfoHolder> hostsPerZoneToUpdate = zoneUpdateOrderService.getHostsPerZoneToUpdate().get(zone);
      List<String> hostsPerZoneToDelete = zoneUpdateOrderService.getHostsPerZoneToDelete().get(zone);
      if (hostsPerZoneToCreate == null && hostsPerZoneToUpdate == null && hostsPerZoneToDelete == null) {
        log.debug("No changes found for zone: {}", zone);
        continue;
      }
      try {
        provider.patch(zone, hostsPerZoneToCreate, hostsPerZoneToUpdate, hostsPerZoneToDelete);
        if (hostsPerZoneToUpdate != null) {
          zoneUpdateOrderService.afterUpdate(hostsPerZoneToUpdate.toArray(new HostInfoHolder[0]));
        }
      } catch (ProviderException e) {
        log.error("Error while updating zone: {}", zone, e);
      }
    }
  }
}
