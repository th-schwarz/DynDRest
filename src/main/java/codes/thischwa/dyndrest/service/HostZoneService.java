package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.repository.HostJdbcDao;
import codes.thischwa.dyndrest.repository.ZoneJdbcDao;
import java.util.List;
import java.util.Optional;
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
  public List<FullHost> getConfiguredHosts() {
    return hostDao.getAllExtended();
  }

  /**
   * Checks if the specified full host exists in the list of configured hosts.
   *
   * @param fullHost the full host to check
   * @return true if the host exists in the list of configured hosts, false otherwise
   */
  public boolean hostExists(String fullHost) {
    try {
      return Optional.ofNullable(hostDao.getByFullHost(fullHost)).isPresent();
    } catch (EmptyResultDataAccessException e) {
      return false;
    }
  }

  /**
   * Retrieves the FullHost object for the given fullHost.
   *
   * @param fullHost the full host to retrieve
   * @return the FullHost object for the given fullHost
   * @throws EmptyResultDataAccessException if no FullHost object is found for the given fullHost
   */
  public FullHost getHost(String fullHost) throws EmptyResultDataAccessException {
    return hostDao.getByFullHost(fullHost);
  }
}
