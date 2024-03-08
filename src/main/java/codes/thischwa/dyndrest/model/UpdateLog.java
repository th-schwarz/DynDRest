package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateLog extends AbstractJdbcEntity {

  private Integer hostId;

  private @Nullable String ipv4;

  private @Nullable String ipv6;

  private @Nullable LocalDateTime changedUpdate;

  private Status status = Status.NEW;

  public static UpdateLog getInstance(Integer hostId, IpSetting ipSetting) {
    return getInstance(hostId, ipSetting, Status.NEW, null, LocalDateTime.now());
  }

  public static UpdateLog getInstance(
      Integer hostId,
      IpSetting ipSetting,
      Status status,
      @Nullable LocalDateTime changedUpdate,
      LocalDateTime changed) {
    UpdateLog updateLog = new UpdateLog();
    updateLog.setHostId(hostId);
    updateLog.setIpv4(ipSetting.ipv4ToString());
    updateLog.setIpv6(ipSetting.ipv6ToString());
    updateLog.setChangedUpdate(changedUpdate);
    updateLog.setStatus(status);
    return updateLog;
  }

  public enum Status {
    FAILED,
    SUCCESS,
    NEW
  }
}
