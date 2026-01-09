package codes.thischwa.dyndrest.server.config;

import static codes.thischwa.dyndrest.server.config.Roles.ROLE_HOST;
import static codes.thischwa.dyndrest.server.config.SecurityConfig.PASSWORD_ENCODER;

import codes.thischwa.dyndrest.model.HostEnriched;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;

/**
 * Manages dynamic authentication for host-specific routes.
 * Each host gets its own authentication where hostname=user and apiToken=password.
 */
@Component
@Slf4j
public class DynamicSecurityChainManager {

  private final InMemoryUserDetailsManager userDetailsManager;

  // Track which hosts are registered to avoid duplicates
  private final Map<String, String> registeredHosts = new HashMap<>();

  /**
   * Creates a new DynamicSecurityChainManager.
   *
   * @param userDetailsManager the user details manager to register host-specific users
   */
  public DynamicSecurityChainManager(InMemoryUserDetailsManager userDetailsManager) {
    this.userDetailsManager = userDetailsManager;
  }

  /**
   * Adds or updates authentication for a specific host.
   * The hostname becomes the username and the apiToken becomes the password.
   *
   * @param hosts the host enriched object containing hostname and apiToken
   */
  public void addOrUpdateHost(HostEnriched... hosts) {
    for (HostEnriched host : hosts) {
      String username = host.getFullHost();
      String password = host.getApiToken();

      log.debug("Attempting to register host: {} with apiToken: {}", username,
          password.substring(0, Math.min(4, password.length())) + "***");

      if (registeredHosts.containsKey(username)) {
        // Update existing user
        if (userDetailsManager.userExists(username)) {
          userDetailsManager.deleteUser(username);
        }
        registeredHosts.remove(username);
      }

      // Create new user with HOST role
      UserDetails userDetails = User.builder()
          .passwordEncoder(PASSWORD_ENCODER::encode)
          .username(username)
          .password(password)
          .roles(ROLE_HOST)
          .build();

      userDetailsManager.createUser(userDetails);
      registeredHosts.put(username, password);
      log.info("Host authentication registered: {} with role {}", username, ROLE_HOST);
    }
  }

  /**
   * Removes authentication for a specific host.
   *
   * @param fullHost the full hostname
   */
  public void removeHost(String fullHost) {
    if (registeredHosts.containsKey(fullHost)) {
      if (userDetailsManager.userExists(fullHost)) {
        userDetailsManager.deleteUser(fullHost);
        log.info("Host authentication removed: {}", fullHost);
      }
      registeredHosts.remove(fullHost);
    }
  }

  /**
   * Checks if a host is registered.
   *
   * @param fullHost the full hostname
   * @return true if the host is registered, false otherwise
   */
  public boolean isHostRegistered(String fullHost) {
    return registeredHosts.containsKey(fullHost);
  }
}
