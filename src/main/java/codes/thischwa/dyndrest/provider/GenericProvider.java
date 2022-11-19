package codes.thischwa.dyndrest.provider;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.util.NetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class GenericProvider implements Provider {

	@Override
	public IpSetting info(String host) throws ProviderException {
		try {
			return NetUtil.resolve(host);
		} catch (IOException e) {
			log.error(host + " couldn't be resolved!", e);
			throw new ProviderException(e);
		}
	}

	@Override
	public void updateBeforeHook(String host, IpSetting ipSetting) throws UpdateHookException {
		// can be implemented from the deriving class
	}

	@Override
	public void updateAfterHook(String host, IpSetting ipSetting) throws UpdateHookException {
		// can be implemented from the deriving class
	}

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
