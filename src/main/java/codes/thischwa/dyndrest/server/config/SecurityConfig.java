package codes.thischwa.dyndrest.server.config;

import codes.thischwa.dyndrest.model.config.AppConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * The security configuration, mainly to specify the authentication for different routes.
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {
  private static final List<String> PUBLIC_ENDPOINTS = new ArrayList<>(List.of("/", "/favicon.ico", "/error"));
  private final AppConfig appConfig;
  static final PasswordEncoder PASSWORD_ENCODER =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();
  private static final String[] LOG_UI_ENDPOINTS = {"/log-ui", "/log-ui/*"};
  private static final String ADMIN_ENDPOINT = "/admin/**";

  private final boolean updateLogEnabled;

  private final boolean adminEnabled;

  @Value("${spring.security.user.name}")
  private String userName;

  @Value("${spring.security.user.password}")
  private String password;

  @Value("${spring.h2.console.enabled}")
  private boolean h2ConsoleEnabled;

  private final boolean healthEnabled;

  /**
   * Constructs a SecurityConfig object with the given AppConfig and Environment.
   *
   * @param appConfig     The AppConfig object containing application configuration.
   * @param env           The Environment object containing environment-specific information.
   * @param healthAccess  The health endpoint access setting.
   */
  public SecurityConfig(AppConfig appConfig, Environment env,
      @Value("${management.endpoint.health.access}") String healthAccess) {
    this.appConfig = appConfig;

    healthEnabled = !"none".equals(healthAccess);

    // check if credentials for addOrUpdate-log-view exist
    boolean isUpdateLogCredentialsEmpty = !StringUtils.hasText(appConfig.updateLogUserName())
        || !StringUtils.hasText(appConfig.updateLogUserPassword());
    updateLogEnabled = appConfig.updateLogPageEnabled() && !isUpdateLogCredentialsEmpty;

    // check if credentials for admin exit
    adminEnabled = StringUtils.hasText(appConfig.adminUserName())
        && StringUtils.hasText(appConfig.adminUserPassword());

    if (Arrays.asList(env.getActiveProfiles()).contains("opendoc")) {
      PUBLIC_ENDPOINTS.add("/v3/api-docs*");
    }
    log.info("Public paths: {}", String.join(",", PUBLIC_ENDPOINTS));
  }

  /**
   * Instantiates the UserDetailsService with different users reading from the properties.
   * Returns an InMemoryUserDetailsManager to allow dynamic user management for host-specific authentication.
   *
   * @return the InMemoryUserDetailsManager
   */
  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    InMemoryUserDetailsManager userManager = new InMemoryUserDetailsManager();
    build(userManager, userName, password, Roles.ROLE_USER);
    if (updateLogEnabled) {
      build(userManager, appConfig.updateLogUserName(), appConfig.updateLogUserPassword(),
          Roles.ROLE_LOGVIEWER);
    }
    if (healthEnabled) {
      build(userManager, appConfig.healthCheckUserName(), appConfig.healthCheckUserPassword(),
          Roles.ROLE_HEALTH);
    }
    if (adminEnabled) {
      build(userManager, appConfig.adminUserName(), appConfig.adminUserPassword(), Roles.ROLE_ADMIN);
    }
    return userManager;
  }

  private void build(UserDetailsManager udm, @Nullable String userName, @Nullable String password,
      String role) {
    if (userName == null || password == null) {
      return;
    }
    udm.createUser(User.builder().passwordEncoder(PASSWORD_ENCODER::encode).username(userName)
        .password(password).roles(role).build());
    log.info("User [{}] with role [{}] created.", userName, role);
  }

  /**
   * Specify different authentications for different routes.
   *
   * @param http the http
   * @return the security filter chain
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    // csrf not necessary for request-based authentication
    http.csrf(AbstractHttpConfigurer::disable);

    if (h2ConsoleEnabled) {
      // h2 settings
      http.authorizeHttpRequests(
              auth -> auth.requestMatchers("/h2-console/**").permitAll())
          .headers(
              headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
    }

    if (updateLogEnabled) {
      // enable security for the log-view
      http.authorizeHttpRequests(
          req -> req.requestMatchers(LOG_UI_ENDPOINTS).hasAnyRole(Roles.ROLE_LOGVIEWER));
    }

    if (healthEnabled) {
      // enable security for the health check, all other management endpoints are disabled
      http.authorizeHttpRequests(
          req -> req.requestMatchers("/manage/health/**").hasAnyRole(Roles.ROLE_HEALTH));
    }

    if (adminEnabled) {
      // enables security for the admin paths
      http.authorizeHttpRequests(req -> req.requestMatchers(ADMIN_ENDPOINT).hasRole(Roles.ROLE_ADMIN))
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    // host-specific authentication for /router/{host} routes
    // hostname=user, apiToken=password, requires ROLE_HOST
    http.authorizeHttpRequests(
        req -> req.requestMatchers("/router/**").hasAnyRole(Roles.ROLE_HOST));

    // public routes
    http.authorizeHttpRequests(
        req -> req.requestMatchers(PUBLIC_ENDPOINTS.toArray(new String[0])).permitAll());

    // enable basic-auth and ROLE_USER for all other routes
    // it's a rest-api, so there is no need for session handling and csrf
    http.authorizeHttpRequests(req -> req.anyRequest().hasAnyRole(Roles.ROLE_USER))
        .httpBasic(Customizer.withDefaults()).sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

}
