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

  private final HostOrderService hostOrderService;
  private final Provider provider;

  public ZoneUpdaterScheduler(HostOrderService hostOrderService, Provider provider) {
    this.hostOrderService = hostOrderService;
    this.provider = provider;
  }

  @Scheduled(fixedDelayString = "${dyndrest.update-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  void process() {
    // TODO must be processed per zone
    List<HostInfoHolder> hostsToUpdate = hostOrderService.getHostsPerZoneToUpdate().values().stream().flatMap(List::stream).toList();
    List<String> hostsToDelete = hostOrderService.getHostsPerZoneToDelete().values().stream().flatMap(List::stream).toList();

    if (hostsToUpdate.isEmpty() && hostsToDelete.isEmpty()) {
      log.debug("No zone updates found.");
      return;
    }

    log.info("Processing zone updates: {} hosts to update, {} hosts to delete",
        hostsToUpdate.size(), hostsToDelete.size());

    for (HostInfoHolder host : hostsToUpdate) {
      try {
        provider.processUpdate(host.getFullHost(), host.getIpSetting());
        log.info("Successfully updated host: {}", host.getFullHost());
      } catch (ProviderException e) {
        log.error("Failed to update host: {}", host.getFullHost(), e);
      }
    }

    for (String host : hostsToDelete) {
      try {
        provider.removeHostIpSettings(host);
        log.info("Successfully deleted host: {}", host);
      } catch (ProviderException e) {
        log.error("Failed to delete host: {}", host, e);
      }
    }

    hostOrderService.afterUpdate(hostsToUpdate.toArray(new HostInfoHolder[0]));
  }
}
