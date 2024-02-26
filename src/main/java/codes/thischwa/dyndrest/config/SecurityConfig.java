package codes.thischwa.dyndrest.config;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/** The security configuration, mainly to specify the authentication9 for different routes. */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

  static final String ROLE_LOGVIEWER = "LOGVIEWER";
  static final String ROLE_USER = "USER";
  static final String ROLE_HEALTH = "HEALTH";
  private final AppConfig appConfig;
  private final PasswordEncoder encoder =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Value("${spring.security.user.name}")
  private String userName;

  @Value("${spring.security.user.password}")
  private String password;

  @Value("${spring.h2.console.enabled}")
  private boolean h2ConsoleEnabled;

  @Value("${management.endpoints.web.base-path}")
  private String managementBasePath;

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

    // public routes
    http.authorizeHttpRequests(
        req ->
            req.requestMatchers(buildMatchers("/", "/favicon.ico", "/v3/api-docs*")).permitAll());

    // enable security for the log-view
    http.authorizeHttpRequests(
        req -> req.requestMatchers(buildMatchers("/log")).hasAnyRole(ROLE_LOGVIEWER));

    // enable security for the health check
    http.authorizeHttpRequests(
        req ->
            req.requestMatchers(buildMatchers(managementBasePath + "/health"))
                .hasAnyRole(ROLE_HEALTH));

    // enable basic-auth and ROLE_USER for all other routes
    http.authorizeHttpRequests(req -> req.anyRequest().hasAnyRole(ROLE_USER))
        .httpBasic(Customizer.withDefaults());

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
