package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HostOrderService {

  private final UpdateLogService updateLogService;

  private Map<String, HostInfoHolder> currentHosts = new ConcurrentHashMap<>();

  @Getter
  private List<HostInfoHolder> hostsToUpdate = Collections.synchronizedList(new ArrayList<>());

  @Getter
  private List<String> hostsToDelete = Collections.synchronizedList(new ArrayList<>());

  public HostOrderService(UpdateLogService updateLogService) {
    this.updateLogService = updateLogService;
  }

  public Collection<HostInfoHolder> getCurrentHosts() {
    return currentHosts.values();
  }

  public void removeFromCurrentHosts(String host) {
    currentHosts.remove(host);
    hostsToDelete.add(host);
  }

  public void addOrUpdateHost(HostInfoHolder... hosts) {
    for (HostInfoHolder host : hosts) {
      if (!currentHosts.containsKey(host.getFullHost())) {
        hostsToUpdate.add(host);
        log.debug("Added new host: {}", host);
        return;
      }
      HostInfoHolder existing = currentHosts.get(host.getFullHost());
      if (!existing.getIpSetting().equals(host.getIpSetting())) {
        hostsToUpdate.add(host);
        log.debug("Added host to update: {}", host);
      } else {
        log.debug("Host already up to date: {}", host);
      }
      updateLogService.log(host.getFullHost(), host.getIpSetting(), UpdateLog.Status.success);
    }
  }

  public void deleteHost(String... hosts) {
    hostsToDelete.addAll(Arrays.asList(hosts));
  }

  public void afterUpdate(HostInfoHolder... hosts) {
    for (HostInfoHolder host : hosts) {
      String fullHost = host.getFullHost();
      currentHosts.put(fullHost, host);
      hostsToUpdate.removeIf(h -> h.getFullHost().equals(fullHost));
    }
    hostsToDelete.clear();
  }

  public boolean hostExists(String host) {
    return currentHosts.containsKey(host);
  }
}
