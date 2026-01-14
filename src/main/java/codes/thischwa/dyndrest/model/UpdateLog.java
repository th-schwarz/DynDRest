package codes.thischwa.dyndrest.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

/** The UpdateLog class represents a log entry for a zone addOrUpdate operation. */
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
   * @param status The status of the addOrUpdate log entry.
   * @param changedUpdate The date and time of the changed addOrUpdate, can be null.
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

  /** The Status enum represents the possible statuses for an addOrUpdate log entry. */
  public enum Status {
    failed,
    waiting,
    success
  }
}
