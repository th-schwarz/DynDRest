package codes.thischwa.dyndrest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DisplayName("Integration tests: controller - router-addOrUpdate")
@Slf4j
public class RouterControllerMockTest extends AbstractControllerMockTest {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("dyndrest.zone-update-scheduler-enabled", () -> true);
  }

  @Test
  void routerUpdateHost_successfulUpdate() throws Exception {
    String host = "valid-host";
    InetAddress ipv4 = InetAddress.getByName("192.168.1.1");
    InetAddress ipv6 = InetAddress.getByName("::1");
    IpSetting setting = new IpSetting(ipv4, ipv6);
    HostInfoHolder holder = new HostInfoHolder();
    holder.setSld(host);
    holder.setIpSetting(setting);
    HttpServletRequest req = mock(HttpServletRequest.class);
    UserDetails userDetails = mock(UserDetails.class);

    when(userDetails.getUsername()).thenReturn(host);
    when(hostZoneService.getHost(host)).thenReturn(Optional.of(holder));
    when(dynamicSecurityChainManager.isHostRegistered(host)).thenReturn(true);

    ResponseEntity<Void> response = routerController.routerUpdateHost(userDetails, host, ipv4, ipv6, req);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(zoneUpdaterService, times(1)).addOrUpdateHost(any(HostInfoHolder.class));
  }

  @Test
  void routerUpdateHost_unauthorizedUser() throws Exception {
    String host = "valid-host";
    InetAddress ipv4 = InetAddress.getByName("192.168.1.1");
    InetAddress ipv6 = InetAddress.getByName("::1");
    HttpServletRequest req = mock(HttpServletRequest.class);
    UserDetails userDetails = mock(UserDetails.class);

    when(userDetails.getUsername()).thenReturn("unauthorized-user");
    when(hostZoneService.getHost(host)).thenReturn(Optional.of(mock(HostEnriched.class)));
    when(dynamicSecurityChainManager.isHostRegistered(host)).thenReturn(true);

    ResponseEntity<Void> response = routerController.routerUpdateHost(userDetails, host, ipv4, ipv6, req);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    //verify(provider, never()).processUpdate(anyString(), any(IpSetting.class));
  }

  @Test
  void routerUpdateHost_hostNotFound() throws Exception {
    String host = "nonexistent-host";
    InetAddress ipv4 = InetAddress.getByName("192.168.1.1");
    InetAddress ipv6 = InetAddress.getByName("::1");
    HttpServletRequest req = mock(HttpServletRequest.class);
    UserDetails userDetails = mock(UserDetails.class);

    when(userDetails.getUsername()).thenReturn(host);
    when(hostZoneService.getHost(host)).thenReturn(Optional.empty());
    when(dynamicSecurityChainManager.isHostRegistered(host)).thenReturn(false);

    ResponseEntity<Void> response = routerController.routerUpdateHost(userDetails, host, ipv4, ipv6, req);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
   //_ verify(provider, never()).processUpdate(anyString(), any(IpSetting.class));
  }
}
