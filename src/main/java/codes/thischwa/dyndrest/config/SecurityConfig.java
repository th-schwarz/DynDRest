package codes.thischwa.dyndrest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	static final String ROLE_LOGVIEWER = "LOGVIEWER";
	static final String ROLE_USER = "USER";
	private final AppConfig ddAutoConfig;
	private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
	@Value("${spring.security.user.name}") private String userName;
	@Value("${spring.security.user.password}") private String password;

	public SecurityConfig(AppConfig ddAutoConfig) {
		this.ddAutoConfig = ddAutoConfig;
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {
		InMemoryUserDetailsManager userManager = new InMemoryUserDetailsManager();
		userManager.createUser(build(userName, password, ROLE_USER));
		if(ddAutoConfig.getUpdateLogUserName() != null && ddAutoConfig.getUpdateLogUserPassword() != null) {
			userManager.createUser(build(ddAutoConfig.getUpdateLogUserName(), ddAutoConfig.getUpdateLogUserPassword(), ROLE_LOGVIEWER));
		}
		return userManager;
	}

	private UserDetails build(String user, String password, String role) {
		return User.builder().passwordEncoder(encoder::encode).username(user).password(password).roles(role).build();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// disable security for greeting and open-api endpoint
				.authorizeRequests().antMatchers("/", "/favicon.ico", "/v3/api-docs*").permitAll().and()

				// enable security for the log-view
				.authorizeRequests().antMatchers("/log").hasAnyRole(ROLE_LOGVIEWER).and()

				// enable basic-auth for all other routes
				.authorizeRequests().anyRequest().hasAnyRole(ROLE_USER).and().httpBasic();

		return http.build();
	}

}
