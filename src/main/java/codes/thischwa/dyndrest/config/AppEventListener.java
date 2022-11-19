package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.provider.Provider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppEventListener {

	private final AppConfig conf;

	private final Provider provider;

	public AppEventListener(AppConfig conf, Provider provider) {
		this.conf = conf;
		this.provider = provider;
	}

	@EventListener(ApplicationReadyEvent.class)
	public final void onApplicationReady() {
		if(conf.isHostValidationEnabled()) {
			provider.validateHostConfiguration();
		}
	}
}
