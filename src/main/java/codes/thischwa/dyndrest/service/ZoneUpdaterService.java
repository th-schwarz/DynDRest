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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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
  private final HostZoneService hostZoneService;
  private final ReentrantLock lock = new ReentrantLock();

  private final Map<String, HostInfoHolder> currentHosts = new ConcurrentHashMap<>();

  private final Map<String, List<HostInfoHolder>> hostsPerZoneToCreate = new ConcurrentHashMap<>();

  private final Map<String, List<HostInfoHolder>> hostsPerZoneToUpdate = new ConcurrentHashMap<>();

  private final Map<String, List<String>> hostsPerZoneToDelete = new ConcurrentHashMap<>();

  /**
   * Constructor for the ZoneUpdaterService class.
   *
   * @param updateLogService the service responsible for logging update actions
   * @param hostZoneService  the service responsible for checking configured hosts
   */
  public ZoneUpdaterService(UpdateLogService updateLogService, HostZoneService hostZoneService) {
    this.updateLogService = updateLogService;
    this.hostZoneService = hostZoneService;
  }

  /**
   * Retrieves all currently tracked hosts.
   *
   * @return a collection of all current hosts
   */
  public Collection<HostInfoHolder> getCurrentHosts() {
    return withLock(() -> List.copyOf(currentHosts.values()));
  }

  /**
   * Adds hosts to the current hosts map.
   *
   * @param hosts the hosts to add
   */
  public void addCurrentHosts(HostInfoHolder... hosts) {
    runExclusive(() -> {
      for (HostInfoHolder host : hosts) {
        currentHosts.put(host.getFullHost(), host);
      }
    });
  }

  /**
   * Removes a host from the current hosts map.
   *
   * @param host the full host name to remove
   */
  public void removeFromCurrentHosts(String host) {
    runExclusive(() -> currentHosts.remove(host));
  }

  /**
   * Adds new hosts or updates existing ones. If a host does not exist, it is marked for creation.
   * If a host exists but has a different IP setting, it is marked for update.
   *
   * @param hosts the hosts to add or update
   */
  public void addOrUpdateHost(HostInfoHolder... hosts) {
    runExclusive(() -> {
      for (HostInfoHolder host : hosts) {
        if (!hostZoneService.hostExists(host.getFullHost())) {
          throw new IllegalArgumentException("Host not found: " + host.getFullHost());
        }
      }
      for (HostInfoHolder host : hosts) {
        String zoneStr = host.getZone();
        if (!hostExists(host.getFullHost())) {
          List<HostInfoHolder> hostsToCreate = hostsPerZoneToCreate.computeIfAbsent(zoneStr, k -> new ArrayList<>());
          hostsToCreate.removeIf(h -> h.getFullHost().equals(host.getFullHost()));
          hostsToCreate.add(host);
          log.debug("Added new host: {}", host);
        } else {
          HostInfoHolder existing = currentHosts.get(host.getFullHost());
          if (!existing.getIpSetting().equals(host.getIpSetting())) {
            List<HostInfoHolder> hostsToUpdate = hostsPerZoneToUpdate.computeIfAbsent(zoneStr, k -> new ArrayList<>());
            hostsToUpdate.removeIf(h -> h.getFullHost().equals(host.getFullHost()));
            hostsToUpdate.add(host);
            log.debug("Added host to addOrUpdate: {}", host);
          } else {
            log.debug("Host already up to date: {}", host);
          }
        }
      }
    });
  }

  /**
   * Marks hosts for deletion. If a host was previously marked for creation or update,
   * it is removed from those lists.
   *
   * @param hosts the full host names to delete
   */
  public void deleteHost(String... hosts) {
    runExclusive(() -> {
      for (String host : hosts) {
        String zone = ZoneStringUtil.getZoneName(host);
        hostsPerZoneToDelete.computeIfAbsent(zone, k -> new ArrayList<>()).add(host);
        removeHost(hostsPerZoneToCreate, zone, host);
        removeHost(hostsPerZoneToUpdate, zone, host);
      }
    });
  }

  /**
   * Removes a deleted host from all pending work without scheduling a provider deletion.
   * Pending updates are recorded as failed while the host still exists in the database.
   *
   * @param host the full host name to remove
   */
  public void purgeHost(String host) {
    runExclusive(() -> {
      String zone = ZoneStringUtil.getZoneName(host);
      logPendingAsFailed(hostsPerZoneToCreate.get(zone), host);
      logPendingAsFailed(hostsPerZoneToUpdate.get(zone), host);
      removeHost(hostsPerZoneToCreate, zone, host);
      removeHost(hostsPerZoneToUpdate, zone, host);
      List<String> toDelete = hostsPerZoneToDelete.get(zone);
      if (toDelete != null) {
        toDelete.removeIf(host::equals);
        if (toDelete.isEmpty()) {
          hostsPerZoneToDelete.remove(zone);
        }
      }
      currentHosts.remove(host);
    });
  }

  private void logPendingAsFailed(@Nullable List<HostInfoHolder> hosts, String fullHost) {
    if (hosts != null) {
      hosts.stream().filter(h -> h.getFullHost().equals(fullHost))
          .forEach(h -> updateLogService.log(fullHost, h.getIpSetting(), UpdateLog.Status.failed));
    }
  }

  private void removeHost(Map<String, List<HostInfoHolder>> hostsByZone, String zone, String host) {
    List<HostInfoHolder> hosts = hostsByZone.get(zone);
    if (hosts != null) {
      hosts.removeIf(h -> h.getFullHost().equals(host));
      if (hosts.isEmpty()) {
        hostsByZone.remove(zone);
      }
    }
  }

  /**
   * Removes a host from all pending work and the current host map, and logs a failed update.
   *
   * @param host the discarded host
   */
  public void discardHost(HostInfoHolder host) {
    runExclusive(() -> {
      String fullHost = host.getFullHost();
      String zone = host.getZone();
      removeHost(hostsPerZoneToCreate, zone, fullHost);
      removeHost(hostsPerZoneToUpdate, zone, fullHost);
      List<String> toDelete = hostsPerZoneToDelete.get(zone);
      if (toDelete != null) {
        toDelete.removeIf(fullHost::equals);
        if (toDelete.isEmpty()) {
          hostsPerZoneToDelete.remove(zone);
        }
      }
      currentHosts.remove(fullHost);
      try {
        updateLogService.log(fullHost, host.getIpSetting(), UpdateLog.Status.failed);
      } catch (IllegalArgumentException e) {
        log.debug("Cannot log discarded update for deleted host: {}", fullHost);
      }
    });
  }

  /**
   * Called after a successful host update. Adds the host to current hosts,
   * removes it from the update and delete lists, and logs the update.
   *
   * @param hosts the hosts that were successfully updated
   */
  public void afterUpdate(HostInfoHolder... hosts) {
    runExclusive(() -> {
      for (HostInfoHolder host : hosts) {
        String fullHost = host.getFullHost();
        if (!hostZoneService.hostExists(fullHost)) {
          discardHost(host);
          continue;
        }
        currentHosts.put(fullHost, host);
        removeAppliedCreate(host);
        removeAppliedUpdate(host);
        updateLogService.log(fullHost, host.getIpSetting(), UpdateLog.Status.success);

        List<String> hostsToDelete = hostsPerZoneToDelete.get(host.getZone());
        if (hostsToDelete != null) {
          hostsToDelete.remove(fullHost);
          if (hostsToDelete.isEmpty()) {
            hostsPerZoneToDelete.remove(host.getZone());
          }
        }
      }
    });
  }

  private void removeAppliedCreate(HostInfoHolder applied) {
    String zone = applied.getZone();
    String fullHost = applied.getFullHost();
    List<HostInfoHolder> creates = hostsPerZoneToCreate.get(zone);
    if (creates == null) {
      return;
    }
    List<HostInfoHolder> newerCreates = creates.stream()
        .filter(h -> h.getFullHost().equals(fullHost) && !h.getIpSetting().equals(applied.getIpSetting()))
        .toList();
    creates.removeIf(h -> h.getFullHost().equals(fullHost));
    if (creates.isEmpty()) {
      hostsPerZoneToCreate.remove(zone);
    }
    for (HostInfoHolder newerCreate : newerCreates) {
      if (!newerCreate.getIpSetting().equals(applied.getIpSetting())) {
        List<HostInfoHolder> updates = hostsPerZoneToUpdate.computeIfAbsent(zone, k -> new ArrayList<>());
        updates.removeIf(h -> h.getFullHost().equals(fullHost));
        updates.add(newerCreate);
      }
    }
  }

  private void removeAppliedUpdate(HostInfoHolder applied) {
    String zone = applied.getZone();
    List<HostInfoHolder> updates = hostsPerZoneToUpdate.get(zone);
    if (updates != null) {
      updates.removeIf(h -> h.getFullHost().equals(applied.getFullHost())
          && h.getIpSetting().equals(applied.getIpSetting()));
      if (updates.isEmpty()) {
        hostsPerZoneToUpdate.remove(zone);
      }
    }
  }

  /**
   * Clears successfully deleted hosts from the tracked state and pending delete list.
   *
   * @param zone  the zone whose delete operation completed
   * @param hosts the full host names that were deleted
   */
  public void afterDelete(String zone, List<String> hosts) {
    runExclusive(() -> {
      hosts.forEach(currentHosts::remove);
      List<String> toDelete = hostsPerZoneToDelete.get(zone);
      if (toDelete != null) {
        toDelete.removeAll(hosts);
        if (toDelete.isEmpty()) {
          hostsPerZoneToDelete.remove(zone);
        }
      }
    });
  }

  /**
   * Checks if a host exists in the current hosts map.
   *
   * @param host the full host name to check
   * @return true if the host exists, false otherwise
   */
  public boolean hostExists(String host) {
    return withLock(() -> currentHosts.containsKey(host));
  }

  /**
   * Returns a safe snapshot of hosts pending creation in the specified zone.
   */
  public List<HostInfoHolder> getHostsToCreate(String zone) {
    return withLock(() -> List.copyOf(hostsPerZoneToCreate.getOrDefault(zone, List.of())));
  }

  /**
   * Returns a safe snapshot of hosts pending update in the specified zone.
   */
  public List<HostInfoHolder> getHostsToUpdate(String zone) {
    return withLock(() -> List.copyOf(hostsPerZoneToUpdate.getOrDefault(zone, List.of())));
  }

  /**
   * Returns a safe snapshot of hosts pending deletion in the specified zone.
   */
  public List<String> getHostsToDelete(String zone) {
    return withLock(() -> List.copyOf(hostsPerZoneToDelete.getOrDefault(zone, List.of())));
  }

  /**
   * Runs an action exclusively with respect to queue mutations and other exclusive actions.
   */
  public void runExclusive(Runnable action) {
    lock.lock();
    try {
      action.run();
    } finally {
      lock.unlock();
    }
  }

  private <T> T withLock(Supplier<T> action) {
    lock.lock();
    try {
      return action.get();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Retrieves all zones that have pending changes (create, update, or delete).
   *
   * @return a set of zone names with pending changes
   */
  public Set<String> getZones() {
    return withLock(() -> {
      Set<String> zones = new HashSet<>();
      zones.addAll(hostsPerZoneToCreate.keySet());
      zones.addAll(hostsPerZoneToUpdate.keySet());
      zones.addAll(hostsPerZoneToDelete.keySet());
      return zones;
    });
  }

  /**
   * Clears all internal state. This method is intended for testing purposes only.
   */
  public void clearForTesting() {
    runExclusive(() -> {
      hostsPerZoneToCreate.clear();
      hostsPerZoneToUpdate.clear();
      hostsPerZoneToDelete.clear();
      currentHosts.clear();
    });
  }
}
