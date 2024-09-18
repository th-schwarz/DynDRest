package codes.thischwa.dyndrest.server.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import codes.thischwa.dyndrest.model.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

/** The security configuration, mainly to specify the authentication for different routes. */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

  static final String ROLE_ADMIN = "ADMIN";
  static final String ROLE_LOGVIEWER = "LOGVIEWER";
  static final String ROLE_USER = "USER";
  static final String ROLE_HEALTH = "HEALTH";
  public final Environment env;
  private final AppConfig appConfig;
  private final PasswordEncoder encoder =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();

  private static final List<String> publicPaths = new ArrayList<>(List.of("/favicon.ico", "/error"));
  private static final String[] loguiPaths = {"/log-ui", "/log-ui/*"};
  private static final String adminPath = "/admin/**";

  private final boolean updateLogEnabled;

  private final boolean adminEnabled;

  @Value("${spring.security.user.name}")
  private String userName;

  @Value("${spring.security.user.password}")
  private String password;

  @Value("${spring.h2.console.enabled}")
  private boolean h2ConsoleEnabled;

  @Value("${management.endpoint.health.enabled}")
  private boolean healthEnabled;

  /**
   * Constructs a SecurityConfig object with the given AppConfig and Environment.
   *
   * @param appConfig The AppConfig object containing application configuration.
   * @param env The Environment object containing environment-specific information.
   */
  public SecurityConfig(AppConfig appConfig, Environment env) {
    this.appConfig = appConfig;
    this.env = env;

    // check if credentials for update-log-view exists
    boolean isUpdateLogCredentialsEmpty =
        !StringUtils.hasText(appConfig.updateLogUserName())
            || !StringUtils.hasText(appConfig.updateLogUserPassword());
    updateLogEnabled = appConfig.updateLogPageEnabled() && !isUpdateLogCredentialsEmpty;

    // check if credentials for admin exits
    adminEnabled =
        StringUtils.hasText(appConfig.adminUserName())
            && StringUtils.hasText(appConfig.adminUserPassword())
            && StringUtils.hasText(appConfig.adminApiToken());

    if (appConfig.greetingEnabled()) {
      publicPaths.add("/");
    }
    if (Arrays.asList(env.getActiveProfiles()).contains("opendoc")) {
      publicPaths.add("/v3/api-docs*");
    }
    log.info("Public paths: {}", String.join(",", publicPaths));
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
    if (updateLogEnabled) {
      build(
          userManager,
          appConfig.updateLogUserName(),
          appConfig.updateLogUserPassword(),
          ROLE_LOGVIEWER);
    }
    if (healthEnabled) {
      build(
          userManager,
          appConfig.healthCheckUserName(),
          appConfig.healthCheckUserPassword(),
          ROLE_HEALTH);
    }
    if (adminEnabled) {
      build(userManager, appConfig.adminUserName(), appConfig.adminUserPassword(), ROLE_ADMIN);
    }
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
   * Specify different authentications for different routes.
   *
   * @param http the http
   * @return the security filter chain
   * @throws Exception the exception
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    if (h2ConsoleEnabled) {
      // h2 settings
      http.authorizeHttpRequests(
              auth -> auth.requestMatchers(PathRequest.toH2Console()).permitAll())
          .headers(
              headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
          .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()));
    }

    if (updateLogEnabled) {
      // enable security for the log-view
      http.authorizeHttpRequests(
          req -> req.requestMatchers(buildMatchers(loguiPaths)).hasAnyRole(ROLE_LOGVIEWER));
    }

    if (healthEnabled) {
      // enable security for the health check, all other management endpoints are disabled
      http.authorizeHttpRequests(
          req ->
              req.requestMatchers(EndpointRequest.to(HealthEndpoint.class))
                  .hasAnyRole(ROLE_HEALTH));
    }

    if (adminEnabled) {
      // enables security for the admin paths
      http.authorizeHttpRequests(
        req ->
            req.requestMatchers(buildMatchers(adminPath)).hasRole(ROLE_ADMIN))
              .sessionManagement(
                      session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .csrf(AbstractHttpConfigurer::disable);
    }

    // public routes
    http.authorizeHttpRequests(
        req -> req.requestMatchers(buildMatchers(publicPaths.toArray(new String[0]))).permitAll());

    // enable basic-auth and ROLE_USER for all other routes
    // it's a rest-api, so there is no need for session handling and csrf
    http.authorizeHttpRequests(req -> req.anyRequest().hasAnyRole(ROLE_USER))
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

  private RequestMatcher[] buildMatchers(String... patterns) {
    List<AntPathRequestMatcher> matchers = new ArrayList<>(patterns.length);
    for (String pattern : patterns) {
      matchers.add(new AntPathRequestMatcher(pattern));
    }
    return matchers.toArray(new AntPathRequestMatcher[0]);
  }
}
