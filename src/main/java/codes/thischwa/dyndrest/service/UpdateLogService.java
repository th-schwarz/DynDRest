package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.AbstractJdbcEntity;
import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.repository.UpdateLogRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** The service class for managing zone update logs. */
@Service
@Slf4j
public class UpdateLogService {

  private final UpdateLogRepo logRepo;

  private final AppConfig appConfig;

  private final HostZoneService hostZoneService;

  /** The constructor. */
  public UpdateLogService(
      UpdateLogRepo logRepo, AppConfig appConfig, HostZoneService hostZoneService) {
    this.logRepo = logRepo;
    this.appConfig = appConfig;
    this.hostZoneService = hostZoneService;
  }

  Pageable create(int pageNo) {
    return PageRequest.of(
        pageNo, appConfig.updateLogPageSize(), Sort.by(Sort.Direction.DESC, "changed"));
  }

  /**
   * Retrieves a page of FullUpdateLog objects.
   *
   * @param pageNo The page number to retrieve. Starts with 0.
   * @return A Page object containing FullUpdateLog objects.
   */
  public Page<FullUpdateLog> getPage(int pageNo) {
    Pageable pageable = create(pageNo);
    Page<UpdateLog> rawPage = logRepo.findAll(pageable);
    List<Integer> ids = rawPage.getContent().stream().map(AbstractJdbcEntity::getId).toList();
    List<FullUpdateLog> updateLogs = logRepo.findAllFullUpdateLogsByIds(ids);
    return new PageImpl<>(updateLogs, create(pageNo), rawPage.getTotalElements());
  }

  long count() {
    return logRepo.count();
  }

  /**
   * Logs the update of a host with the given IP settings and status.
   *
   * @param host The host to update.
   * @param reqIpSetting The IP settings for the update.
   * @param status The status of the update log entry.
   * @throws IllegalArgumentException If the host is not found.
   */
  public void log(String host, IpSetting reqIpSetting, UpdateLog.Status status) {
    Optional<FullHost> opt = hostZoneService.getHost(host);
    if (opt.isPresent()) {
      FullHost fullHost = opt.get();
      UpdateLog updateLog =
          UpdateLog.getInstance(
              fullHost.getId(), reqIpSetting, status, LocalDateTime.now(), LocalDateTime.now());
      logRepo.save(updateLog);
    } else {
      // shouldn't be happened
      throw new IllegalArgumentException("Host not found: " + host);
    }
  }
}
