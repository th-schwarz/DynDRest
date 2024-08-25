package codes.thischwa.dyndrest.provider;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;

/**
 * Defines the functionality of a dns provider. <br>
 * A new provider implementation should be extended from {@link GenericProvider}. It already
 * implements some basic functions.
 */
@SuppressWarnings("EmptyMethod")
public interface Provider {

  /** Validates the host configuration. */
  void validateHostZoneConfiguration() throws IllegalArgumentException;

  /**
   * Hook before update.
   *
   * @param host the host
   * @param ipSetting the ip setting
   */
  void updateBeforeHook(String host, IpSetting ipSetting) throws UpdateHookException;

  /**
   * Update the desired 'host' with the desired IP setting.
   *
   * @param host the host
   * @param ipSetting the ip setting
   * @throws ProviderException the provider exception
   */
  void update(String host, IpSetting ipSetting) throws ProviderException;

  /**
   * Hook after update.
   *
   * @param host the host
   * @param ipSetting the ip setting
   */
  void updateAfterHook(String host, IpSetting ipSetting) throws UpdateHookException;

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
   * after-hooks. <br>
   * Hint: There is no need to implement this, if the implementation derives from {@link
   * GenericProvider}!
   *
   * @param host the host
   * @param ipSetting the ip setting
   * @throws ProviderException the provider exception
   */
  void processUpdate(String host, IpSetting ipSetting) throws ProviderException;

  void addHost(String zaneName, String host) throws ProviderException;

  void removeHost(String host) throws ProviderException;
}
