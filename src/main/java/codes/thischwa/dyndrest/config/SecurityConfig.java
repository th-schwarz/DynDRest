package codes.thischwa.dyndrest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** The security configuration, mainly to specify the authentication9 for different routes. */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

  static final String ROLE_LOGVIEWER = "LOGVIEWER";
  static final String ROLE_USER = "USER";
  static final String ROLE_HEALTH = "HEALTH";
  private final AppConfig appConfig;
  private final MvcRequestMatcherBuilder mvc;
  private final PasswordEncoder encoder =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Value("${spring.security.user.name}")
  private String userName;

  @Value("${spring.security.user.password}")
  private String password;

  public SecurityConfig(AppConfig appConfig, MvcRequestMatcherBuilder mvc) {
    this.appConfig = appConfig;
    this.mvc = mvc;
  }

  /**
   * Instantiates the UserDetailsService with different users reading from the properties.
   *
   * @return the UserDetailsService
   */
  @Bean
  public UserDetailsService userDetailsService() {
    InMemoryUserDetailsManager userManager = new InMemoryUserDetailsManager();
    build(userManager, userName, password, ROLE_USER);
    build(
        userManager,
        appConfig.updateLogUserName(),
        appConfig.updateLogUserPassword(),
        ROLE_LOGVIEWER);
    build(
        userManager,
        appConfig.healthCheckUserName(),
        appConfig.healthCheckUserPassword(),
        ROLE_HEALTH);
    return userManager;
  }

  private void build(
      UserDetailsManager udm, @Nullable String userName, @Nullable String password, String role) {
    if (userName == null || password == null) {
      return;
    }
    udm.createUser(
        User.builder()
            .passwordEncoder(encoder::encode)
            .username(userName)
            .password(password)
            .roles(role)
            .build());
    log.info("User [{}] with role [{}] created.", userName, role);
  }

  /**
   * Specify different authentications for different routes..
   *
   * @param http the http
   * @return the security filter chain
   * @throws Exception the exception
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http)
      throws Exception {
    http

        // public routes
        .authorizeHttpRequests(
            req ->
                req.requestMatchers(mvc.matchers("/", "/favicon.ico", "/v3/api-docs*")).permitAll())

        // enable security for the log-view
        .authorizeHttpRequests(
            req -> req.requestMatchers(mvc.matchers("/log")).hasAnyRole(ROLE_LOGVIEWER))

        // enable security for the health check
        .authorizeHttpRequests(
            req -> req.requestMatchers(mvc.matchers("/manage/health")).hasAnyRole(ROLE_HEALTH))

        // enable basic-auth and ROLE_USER for all other routes
        .authorizeHttpRequests(req -> req.anyRequest().hasAnyRole(ROLE_USER))
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}
