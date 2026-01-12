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

@Service
@Slf4j
public class ZoneUpdateOrderService {

  private final UpdateLogService updateLogService;

  private final Map<String, HostInfoHolder> currentHosts = new ConcurrentHashMap<>();

  @Getter
  private Map<String, List<HostInfoHolder>> hostsPerZoneToCreate = new ConcurrentHashMap<>();

  @Getter
  private Map<String, List<HostInfoHolder>> hostsPerZoneToUpdate = new ConcurrentHashMap<>();

  @Getter
  private Map<String, List<String>> hostsPerZoneToDelete = new ConcurrentHashMap<>();

  public ZoneUpdateOrderService(UpdateLogService updateLogService) {
    this.updateLogService = updateLogService;
  }

  public Collection<HostInfoHolder> getCurrentHosts() {
    return currentHosts.values();
  }

  public void addCurrentHosts(HostInfoHolder... hosts) {
    for (HostInfoHolder host : hosts) {
      currentHosts.put(host.getFullHost(), host);
    }
  }

  public void removeFromCurrentHosts(String host) {
    currentHosts.remove(host);
  }

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
          log.debug("Added host to update: {}", host);
        } else {
          log.debug("Host already up to date: {}", host);
        }
      }
    }
  }

  public void deleteHost(String... hosts) {
    for (String host : hosts) {
      String zone = ZoneStringUtil.getZoneName(host);
      hostsPerZoneToDelete.computeIfAbsent(zone, k -> new ArrayList<>()).add(host);
      if (hostsPerZoneToCreate.get(zone) != null) {
        hostsPerZoneToCreate.get(zone).removeIf(h -> h.getFullHost().equals(host));
        if (hostsPerZoneToCreate.get(zone).isEmpty()) {
          hostsPerZoneToCreate.remove(zone);
        }
      }
      if (hostsPerZoneToUpdate.get(zone) != null) {
        hostsPerZoneToUpdate.get(zone).removeIf(h -> h.getFullHost().equals(host));
        if (hostsPerZoneToUpdate.get(zone).isEmpty()) {
          hostsPerZoneToUpdate.remove(zone);
        }
      }
    }
  }

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
      if (hostsToDelete != null)
        hostsToDelete.remove(fullHost);
    }
  }

  public boolean hostExists(String host) {
    return currentHosts.containsKey(host);
  }

  public Set<String> getZones() {
    Set<String> zones = new HashSet<>();
    zones.addAll(hostsPerZoneToCreate.keySet());
    zones.addAll(hostsPerZoneToUpdate.keySet());
    zones.addAll(hostsPerZoneToDelete.keySet());
    return zones;
  }
}
