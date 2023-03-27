package codes.thischwa.dyndrest.provider;

import codes.thischwa.dyndrest.model.IpSetting;
import java.util.Set;

/**
 * Defines the functionality of a dns provider. <br> A new provider implementation should be
 * extended from {@link GenericProvider}. It already implements some basic functions.
 */
public interface Provider {

  /**
   * Validates the host configuration.
   */
  void validateHostConfiguration() throws IllegalArgumentException;

  /**
   * Returns all hosts that are configured correctly.
   *
   * @return all hosts that are configured correctly
   */
  Set<String> getConfiguredHosts();

  /**
   * Checks if the desired 'hosts' exists. <br> Hint: There is no need to override it!
   *
   * @param host the host
   * @return the boolean
   */
  default boolean hostExists(String host) {
    return getConfiguredHosts().contains(host);
  }

  /**
   * Hook before update.
   *
   * @param host      the host
   * @param ipSetting the ip setting
   * @throws UpdateHookException if an exceptions happens while before-update-hook
   */
  void updateBeforeHook(String host, IpSetting ipSetting) throws UpdateHookException;

  /**
   * Update the desired 'host' with the desired IP setting.
   *
   * @param host      the host
   * @param ipSetting the ip setting
   * @throws ProviderException the provider exception
   */
  void update(String host, IpSetting ipSetting) throws ProviderException;

  /**
   * Hook after update.
   *
   * @param host      the host
   * @param ipSetting the ip setting
   * @throws UpdateHookException if an exceptions happens while after-update-hook
   */
  void updateAfterHook(String host, IpSetting ipSetting) throws UpdateHookException;

  /**
   * Gets apitoken of the desired 'host'.
   *
   * @param host the host
   * @return the apitoken
   */
  String getApitoken(String host);

  /**
   * Determine the IPs of the 'host'.
   *
   * @param host The host for which the IPs are to be determined.
   * @return IP setting of the 'host'.
   * @throws ProviderException if the IPs couldn't be determined.
   */
  IpSetting info(String host) throws ProviderException;

  /**
   * Updates the desired 'host' with the desired IP setting with consideration of the before- and
   * after-hooks. <br> Hint: There is no need to implement this, if the implementation derives from
   * {@link GenericProvider}!
   *
   * @param host      the host
   * @param ipSetting the ip setting
   * @throws ProviderException the provider exception
   */
  void processUpdate(String host, IpSetting ipSetting) throws ProviderException;
}