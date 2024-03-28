package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

/** The UpdateLog class represents a log entry for a zone update operation. */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateLog extends AbstractJdbcEntity {

  private Integer hostId;

  private @Nullable String ipv4;

  private @Nullable String ipv6;

  private @EqualsAndHashCode.Exclude @Nullable LocalDateTime changedUpdate;

  private Status status = Status.failed;

  /**
   * Returns a new instance of UpdateLog with the specified parameters.
   *
   * @param hostId The host id.
   * @param ipSetting The IP settings.
   * @param status The status of the update log entry.
   * @param changedUpdate The date and time of the changed update, can be null.
   * @param changed The date and time of the log entry creation.
   * @return A new instance of UpdateLog.
   */
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
    updateLog.setChanged(changed);
    updateLog.setStatus(status);
    return updateLog;
  }

  /** The Status enum represents the possible statuses for an update log entry. */
  public enum Status {
    failed,
    success
  }
}
