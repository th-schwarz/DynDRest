package codes.thischwa.dyndrest.model;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * Represents an entity that holds detailed information about a host,
 * extending functionality provided by the {@code HostEnriched} class.
 * This class includes an {@code IpSetting} instance, which specifies
 * the IP configuration for the host.
 *
 * <ul>
 *   <li>Provides methods to compare and calculate the hash code for host
 *       entities, including {@code IpSetting} details.</li>
 *   <li>Extends the {@code equals} and {@code hashCode} implementations of
 *       the {@code HostEnriched} superclass to incorporate comparisons for
 *       the {@code IpSetting} field.</li>
 * </ul>
 */
public class HostInfoHolder extends HostEnriched {

  @Getter
  @Setter
  private IpSetting ipSetting;

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    HostInfoHolder that = (HostInfoHolder) o;
    return java.util.Objects.equals(ipSetting, that.ipSetting);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(super.hashCode(), ipSetting);
  }

  /**
   * Creates a HostInfoHolder from a HostEnriched and IpSetting.
   *
   * @param host      the enriched host
   * @param ipSetting the IP setting
   * @return a new HostInfoHolder instance
   */
  public static HostInfoHolder of(HostEnriched host, IpSetting ipSetting) {
    HostInfoHolder hostInfoHolder = new HostInfoHolder();
    hostInfoHolder.setZoneId(host.getZoneId());
    hostInfoHolder.setZone(host.getZone());
    hostInfoHolder.setNs(host.getNs());
    hostInfoHolder.setSld(host.getSld());
    hostInfoHolder.setIpSetting(ipSetting);
    return hostInfoHolder;
  }
}
