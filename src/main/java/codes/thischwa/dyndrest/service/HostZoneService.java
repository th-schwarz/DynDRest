package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.repository.HostJdbcDao;
import codes.thischwa.dyndrest.repository.ZoneJdbcDao;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/** Service for validating hosts. */
@Service
@Slf4j
public class HostZoneService {
  private final HostJdbcDao hostDao;
  private final ZoneJdbcDao zoneDao;

  public HostZoneService(HostJdbcDao hostDao, ZoneJdbcDao zoneDao) {
    this.hostDao = hostDao;
    this.zoneDao = zoneDao;
  }

  public boolean validate(String fullHost, String apitoken) throws EmptyResultDataAccessException {
    Host host = hostDao.getByFullHost(fullHost);
    return host.getApiToken().equals(apitoken);
  }

  /**
   * Retrieves a list of configured hosts.
   *
   * @return the list of configured hosts
   */
  public List<String> getConfiguredHosts() {
    List<String> hosts = new ArrayList<>();
    for (Host host : hostDao.getAllExtended()) {
      hosts.add(host.getFullHost());
    }
    return hosts;
  }

  public boolean hostExists(String host) {
    return getConfiguredHosts().contains(host);
  }

  public String getPrimaryNameServer(String zone) {
    return zoneDao.getByName(zone).getNs();
  }
}
