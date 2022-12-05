package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.util.NetUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A controller that delivers a page to show the zone update logs.
 */
@Controller
@ConditionalOnProperty(name = "dyndrest.update-log-page-enabled")
public class UpdateLogContoller {

	private final AppConfig config;

	@Value("${spring.security.user.name}") private String basicAuthUser;

	@Value("${spring.security.user.password}") private String basicAuthPassword;

	public UpdateLogContoller(AppConfig config) {
		this.config = config;
	}

	/**
	 * Delivers page to show the zone update logs.
	 * @param model optional model for processing
	 * @return the zone update logs page
	 */
	@GetMapping(value = "/log", produces = MediaType.TEXT_HTML_VALUE)
	public String log(Model model) {
		String baseUrl = NetUtil.getBaseUrl(config.isUpdateLogRestForceHttps());

		model.addAttribute("server_url", baseUrl + "/info/update-log");
		if(basicAuthUser != null && basicAuthPassword != null) {
			String basicAuth = NetUtil.buildBasicAuth(basicAuthUser, basicAuthPassword);
			model.addAttribute("header_basicauth", basicAuth);
			model.addAttribute("page_size", config.getUpdateLogPageSize());
		}
		return "log-view";
	}

}
