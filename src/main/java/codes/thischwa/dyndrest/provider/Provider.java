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
   *     for each host, including IP settings
   * @throws ProviderException if there is an error while retrieving the current configured hosts
   */
  List<HostInfoHolder> getCurrentConfiguredHosts(List<HostEnriched> hostsEnriched) throws ProviderException;

  /**
   * Validates the host configuration.
   */
  void validateHostZoneConfiguration() throws IllegalArgumentException;

  /**
   * Adds a new host with the specified IP settings or updates the IP settings of an existing host.
   *
   * @param fqdn      the host name to be added or updated
   * @param ipSetting the IP settings to be assigned to the specified host
   * @throws ProviderException if an error occurs during the operation
   */
  void addOrUpdate(String fqdn, IpSetting ipSetting) throws ProviderException;

  /**
   * Determine the IPs of the 'host'.
   *
   * @param fqdn The host for which the IPs are to be determined.
   * @return IP setting of the 'host'.
   * @throws ProviderException if the IPs couldn't be determined.
   */
  IpSetting info(String fqdn) throws ProviderException;

  /**
   * Removes the IP settings associated with the specified host.
   *
   * @param fqdn the hostname for which the IP settings are to be removed
   * @throws ProviderException if an error occurs during the removal process
   */
  void removeHostIpSettings(String fqdn) throws ProviderException;

  /**
   * Applies changes to the DNS zone by creating, updating, or deleting host configurations.
   * The method handles three operations:
   * <ul>
   *   <li>Adding new host entries specified in the {@code creates} list.</li>
   *   <li>Updating existing host entries specified in the {@code updates} list.</li>
   *   <li>Deleting host entries based on the fully qualified domain names provided in the {@code deletes} list.</li>
   * </ul>
   *
   * @param zone the DNS zone where the changes should be applied
   * @param creates a list of {@code HostInfoHolder} objects representing new hosts to be added; may be {@code null}
   * @param updates a list of {@code HostInfoHolder} objects representing hosts to be updated; may be {@code null}
   * @param deletes a list of fully qualified domain names (FQDN) representing hosts to be deleted; may be {@code null}
   * @throws ProviderException if an error occurs during any of the create, update, or delete operations
   */
  void patch(String zone, @Nullable List<HostInfoHolder> creates, @Nullable List<HostInfoHolder> updates, @Nullable List<String> deletes)
      throws ProviderException;
}
