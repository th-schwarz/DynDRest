package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.util.ZoneStringUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The ZoneUpdaterService class facilitates management and updates of host information
 * within specific zones. It provides functionality to track currently active hosts,
 * as well as hosts pending creation, update, or deletion, while ensuring changes
 * are logged and synchronized appropriately.
 *
 * <p>This class uses a ConcurrentHashMap for thread-safe operations and integrates with
 * an UpdateLogService to record update logs for hosts.
 *
 * <p>Responsibilities include:
 * <ul>
 * <li>Adding and updating host information.</li>
 * <li>Deleting hosts from tracked zones.</li>
 * <li>Querying current hosts and zones involved in changes.</li>
 * <li>Managing host updates, ensuring they are correctly categorized in terms of creation,
 * update, and deletion.</li>
 * </ul>
 */
@Service
@Slf4j
public class ZoneUpdaterService {

  private final UpdateLogService updateLogService;

  private final Map<String, HostInfoHolder> currentHosts = new ConcurrentHashMap<>();

  @Getter
  private Map<String, List<HostInfoHolder>> hostsPerZoneToCreate = new ConcurrentHashMap<>();

  @Getter
  private Map<String, List<HostInfoHolder>> hostsPerZoneToUpdate = new ConcurrentHashMap<>();

  @Getter
  private Map<String, List<String>> hostsPerZoneToDelete = new ConcurrentHashMap<>();

  /**
   * Constructor for the ZoneUpdaterService class.
   *
   * @param updateLogService the service responsible for logging update actions
   */
  public ZoneUpdaterService(UpdateLogService updateLogService) {
    this.updateLogService = updateLogService;
  }

  /**
   * Retrieves all currently tracked hosts.
   *
   * @return a collection of all current hosts
   */
  public Collection<HostInfoHolder> getCurrentHosts() {
    return currentHosts.values();
  }

  /**
   * Adds hosts to the current hosts map.
   *
   * @param hosts the hosts to add
   */
  public void addCurrentHosts(HostInfoHolder... hosts) {
    for (HostInfoHolder host : hosts) {
      currentHosts.put(host.getFullHost(), host);
    }
  }

  /**
   * Removes a host from the current hosts map.
   *
   * @param host the full host name to remove
   */
  public void removeFromCurrentHosts(String host) {
    currentHosts.remove(host);
  }

  /**
   * Adds new hosts or updates existing ones. If a host does not exist, it is marked for creation.
   * If a host exists but has a different IP setting, it is marked for update.
   *
   * @param hosts the hosts to add or update
   */
  public void addOrUpdateHost(HostInfoHolder... hosts) {
    for (HostInfoHolder host : hosts) {
      String zoneStr = host.getZone();
      if (!hostExists(host.getFullHost())) {
        List<HostInfoHolder> hostsToCreate = hostsPerZoneToCreate.computeIfAbsent(zoneStr, k -> new ArrayList<>());
        hostsToCreate.add(host);
        log.debug("Added new host: {}", host);
      } else {
        HostInfoHolder existing = currentHosts.get(host.getFullHost());
        if (!existing.getIpSetting().equals(host.getIpSetting())) {
          List<HostInfoHolder> hostsToUpdate = hostsPerZoneToUpdate.computeIfAbsent(zoneStr, k -> new ArrayList<>());
          hostsToUpdate.stream().filter(h -> h.getFullHost().equals(host.getFullHost())).findFirst().ifPresent(hostsToUpdate::remove);
          hostsToUpdate.add(host);
          log.debug("Added host to addOrUpdate: {}", host);
        } else {
          log.debug("Host already up to date: {}", host);
        }
      }
    }
  }

  /**
   * Marks hosts for deletion. If a host was previously marked for creation or update,
   * it is removed from those lists.
   *
   * @param hosts the full host names to delete
   */
  public void deleteHost(String... hosts) {
    for (String host : hosts) {
      String zone = ZoneStringUtil.getZoneName(host);
      hostsPerZoneToDelete.computeIfAbsent(zone, k -> new ArrayList<>()).add(host);
      List<HostInfoHolder> toCreate = hostsPerZoneToCreate.get(zone);
      if (toCreate != null) {
        toCreate.removeIf(h -> h.getFullHost().equals(host));
        if (toCreate.isEmpty()) {
          hostsPerZoneToCreate.remove(zone);
        }
      }
      List<HostInfoHolder> toUpdate = hostsPerZoneToUpdate.get(zone);
      if (toUpdate != null) {
        toUpdate.removeIf(h -> h.getFullHost().equals(host));
        if (toUpdate.isEmpty()) {
          hostsPerZoneToUpdate.remove(zone);
        }
      }
    }
  }

  /**
   * Called after a successful host update. Adds the host to current hosts,
   * removes it from the update and delete lists, and logs the update.
   *
   * @param hosts the hosts that were successfully updated
   */
  public void afterUpdate(HostInfoHolder... hosts) {
    for (HostInfoHolder host : hosts) {
      String zoneStr = host.getZone();
      String fullHost = host.getFullHost();
      currentHosts.put(fullHost, host);
      List<HostInfoHolder> hostsToUpdate = hostsPerZoneToUpdate.get(zoneStr);
      if (hostsToUpdate != null) {
        hostsToUpdate.removeIf(h -> h.getFullHost().equals(fullHost));
        if (hostsToUpdate.isEmpty()) {
          hostsPerZoneToUpdate.remove(zoneStr);
        }
      }
      updateLogService.log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.success);

      List<String> hostsToDelete = hostsPerZoneToDelete.get(zoneStr);
      if (hostsToDelete != null) {
        hostsToDelete.remove(fullHost);
      }
    }
  }

  /**
   * Checks if a host exists in the current hosts map.
   *
   * @param host the full host name to check
   * @return true if the host exists, false otherwise
   */
  public boolean hostExists(String host) {
    return currentHosts.containsKey(host);
  }

  /**
   * Retrieves all zones that have pending changes (create, update, or delete).
   *
   * @return a set of zone names with pending changes
   */
  public Set<String> getZones() {
    Set<String> zones = new HashSet<>();
    zones.addAll(hostsPerZoneToCreate.keySet());
    zones.addAll(hostsPerZoneToUpdate.keySet());
    zones.addAll(hostsPerZoneToDelete.keySet());
    return zones;
  }

  /**
   * Clears all internal state. This method is intended for testing purposes only.
   */
  public void clearForTesting() {
    hostsPerZoneToCreate.clear();
    hostsPerZoneToUpdate.clear();
    hostsPerZoneToDelete.clear();
    currentHosts.clear();
  }
}
