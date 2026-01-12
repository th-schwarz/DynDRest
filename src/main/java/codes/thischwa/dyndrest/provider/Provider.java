package codes.thischwa.dyndrest.provider;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Defines the functionality of a dns provider. <br>
 * A new provider implementation should be extended from {@link GenericProvider}. It already
 * implements some basic functions.
 */
public interface Provider {

  /**
   * Confirms the specified DNS zone during configuration validation.
   *
   * @param zone the DNS zone to be confirmed
   */
  void confirmZone(Zone zone);

  /**
   * Retrieves the current configuration of hosts by enriching the provided list of {@code HostEnriched} objects
   * with the IP settings.
   *
   * @param hostsEnriched the list of enriched host entities for which the current configuration
   *                      details need to be retrieved
   * @return a list of {@code HostInfoHolder} objects containing detailed configuration information
   *         for each host, including IP settings
   * @throws ProviderException if there is an error while retrieving the current configured hosts
   */
  List<HostInfoHolder> getCurrentConfiguredHosts(List<HostEnriched> hostsEnriched) throws ProviderException;

  /**
   * Validates the host configuration.
   */
  void validateHostZoneConfiguration() throws IllegalArgumentException;

  /**
   * Update the desired 'host' with the desired IP setting.
   *
   * @param host      the host
   * @param ipSetting the ip setting
   * @throws ProviderException the provider exception
   */
  void update(String host, IpSetting ipSetting) throws ProviderException;

  /**
   * Determine the IPs of the 'host'.
   *
   * @param host The host for which the IPs are to be determined.
   * @return IP setting of the 'host'.
   * @throws ProviderException if the IPs couldn't be determined.
   */
  IpSetting info(String host) throws ProviderException;

  void addHost(String zoneName, String host) throws ProviderException;

  void removeHostIpSettings(String host) throws ProviderException;

  void patch(String zone, @Nullable List<HostInfoHolder> creates, @Nullable List<HostInfoHolder> updates, @Nullable List<String> deletes) throws ProviderException;
}
