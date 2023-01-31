package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLogPage;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.service.UpdateLogCache;
import codes.thischwa.dyndrest.service.UpdateLogger;
import codes.thischwa.dyndrest.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@Slf4j
public class ApiController implements ApiRoutes {

	private final Provider provider;

	private final UpdateLogger updateLogger;

	private final UpdateLogCache updateLogCache;

	public ApiController(Provider provider, UpdateLogger updateLogger, UpdateLogCache updateLogCache) {
		this.provider = provider;
		this.updateLogger = updateLogger;
		this.updateLogCache = updateLogCache;
	}

	@Override
	public void update(String host, String apitoken, InetAddress ipv4, InetAddress ipv6, HttpServletRequest req) {
		log.debug("entered #update: host={}, apitoken={}, ipv4={}, ipv6={}", host, apitoken, ipv4, ipv6);

		// validation
		if(!provider.hostExists(host))
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		if(!validateApitoken(host, apitoken)) {
			log.warn("Unknown apitoken {} for host {}.", apitoken, host);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		IpSetting reqIpSetting = new IpSetting(ipv4, ipv6);
		if(reqIpSetting.isNotSet()) {
			log.debug("Both IP parameters are null, try to fetch the remote IP.");
			String remoteIP = req.getRemoteAddr();
			if(remoteIP == null || !NetUtil.isIP(remoteIP)) {
				log.error("Couldn't determine the remote ip!");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			log.debug("Fetched remote IP: {}", remoteIP);
			try {
				reqIpSetting = new IpSetting(remoteIP);
			} catch (UnknownHostException e) {
				log.error("Remote ip isn't valid: {}", remoteIP);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		}

		// processing the update
		try {
			IpSetting current = provider.info(host);
			if(current.equals(reqIpSetting)) {
				log.debug("IPs haven't changed for {}, no update required!", host);
			} else {
				provider.processUpdate(host, reqIpSetting);
				log.info("Updated host {} successful with: {}", host, reqIpSetting);
				updateLogger.log(host, reqIpSetting);
			}
		} catch (ProviderException e) {
			log.error("Updated host failed: " + host, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<IpSetting> info(String host, @RequestParam String apitoken) {
		log.debug("entered #info: host={}", host);    // validation
		if(!provider.hostExists(host))
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		if(!validateApitoken(host, apitoken)) {
			log.warn("Unknown apitoken {} for host {}.", apitoken, host);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		IpSetting ipSetting;
		try {
			ipSetting = provider.info(host);
		} catch (ProviderException e) {
			log.error("Zone info failed for: " + host, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ResponseEntity.ok(ipSetting);
	}

	@Override
	public ResponseEntity<UpdateLogPage> getLogs(@RequestParam Integer page, @RequestParam(required = false) String search) {
		if(page != null) // grid.js: pagination starts with 0
			page++;
		return ResponseEntity.ok(updateLogCache.getResponsePage(page, search));
	}

	private boolean validateApitoken(String host, String apiToken) {
		return apiToken.equals(provider.getApitoken(host));
	}
}
