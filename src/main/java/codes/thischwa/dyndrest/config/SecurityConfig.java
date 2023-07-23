package codes.thischwa.dyndrest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** The security configuration, mainly to specify the authentications for different routes. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  static final String ROLE_LOGVIEWER = "LOGVIEWER";
  static final String ROLE_USER = "USER";
  private final AppConfig appConfig;
  private final PasswordEncoder encoder =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Value("${spring.security.user.name}")
  private String userName;

  @Value("${spring.security.user.password}")
  private String password;

  public SecurityConfig(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  /**
   * Instantiates the UserDetailsService with different users reading from the properties.
   *
   * @return the UserDetailsService
   */
  @Bean
  public UserDetailsService userDetailsService() {
    InMemoryUserDetailsManager userManager = new InMemoryUserDetailsManager();
    userManager.createUser(build(userName, password, ROLE_USER));
    if (appConfig.updateLogUserName() != null && appConfig.updateLogUserPassword() != null) {
      userManager.createUser(
          build(appConfig.updateLogUserName(), appConfig.updateLogUserPassword(), ROLE_LOGVIEWER));
    }
    return userManager;
  }

  private UserDetails build(String user, String password, String role) {
    return User.builder()
        .passwordEncoder(encoder::encode)
        .username(user)
        .password(password)
        .roles(role)
        .build();
  }

  /**
   * Specify different authentications for different routes..
   *
   * @param http the http
   * @return the security filter chain
   * @throws Exception the exception
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http

        // public routes
        .authorizeHttpRequests()
        .requestMatchers("/", "/favicon.ico", "/v3/api-docs*")
        .permitAll()
        .and()

        // enable security for the log-view
        .authorizeHttpRequests()
        .requestMatchers("/log")
        .hasAnyRole(ROLE_LOGVIEWER)
        .and()

        // enable basic-auth and ROLE_USER for all other routes
        .authorizeHttpRequests()
        .anyRequest()
        .hasAnyRole(ROLE_USER)
        .and()
        .httpBasic();

    return http.build();
  }
}
