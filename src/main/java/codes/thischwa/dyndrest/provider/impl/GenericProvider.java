package codes.thischwa.dyndrest.provider.impl;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.UpdateHookException;
import codes.thischwa.dyndrest.util.NetUtil;
import lombok.extern.slf4j.Slf4j;

/** Generic helper class provider implementations. */
@SuppressWarnings("RedundantThrows")
@Slf4j
public abstract class GenericProvider implements Provider {

  /**
   * Resolves the IP address of the given host via a name server request.<br/>
   * Should be implemented by the deriving class to fetch the real zone data.
   *
   * @param host the host
   * @return the IP setting
   * @throws ProviderException if the resolution fails
   */
  @Override
  public IpSetting info(String host) throws ProviderException {
    return NetUtil.resolve(host);
  }

  @Override
  public void updateBeforeHook(String host, IpSetting ipSetting) throws UpdateHookException {
    // can be implemented from the deriving class
  }

  @Override
  public void updateAfterHook(String host, IpSetting ipSetting) throws UpdateHookException {
    // can be implemented from the deriving class
  }

  /**
   * A wrapper for {@link #update(String, IpSetting)} with respect of hooks before and after.
   *
   * @param host the host
   * @param ipSetting the ip setting
   * @throws ProviderException can be thrown while provider calls
   */
  public final void processUpdate(String host, IpSetting ipSetting) throws ProviderException {
    // TODO reconsider the exception handling for hooks
    try {
      updateBeforeHook(host, ipSetting);
    } catch (UpdateHookException e) {
      log.error("Exception while before-update-hook!", e);
    }
    update(host, ipSetting);
    try {
      updateAfterHook(host, ipSetting);
    } catch (UpdateHookException e) {
      log.error("Exception while after-update-hook!", e);
    }
  }
}
