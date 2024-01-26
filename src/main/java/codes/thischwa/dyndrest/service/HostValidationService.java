package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.repository.HostJdbcDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/** Service for validating hosts. */
@Service
@Slf4j
public class HostValidationService {
  private final HostJdbcDao dao;

  public HostValidationService(HostJdbcDao dao) {
    this.dao = dao;
  }

  public boolean validate(String fullHost, String apitoken) throws EmptyResultDataAccessException {
    Host host = dao.getByFullHost(fullHost);
    return host.getApiToken().equals(apitoken);
  }
}
