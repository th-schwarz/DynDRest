package codes.thischwa.dyndrest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("Integration tests: controller - api-update")
@Slf4j
class ApiControllerUpdateTest extends AbstractControllerTest {

  private final String ipStr = "192.168.1.1";
  private final String validToken = "valid_token";

  @BeforeEach
  void resetMocks() {
    reset(provider, hostZoneService, dynamicSecurityChainManager);
  }

  @Test
  void testSuccess() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testSuccess: {}", host);
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, validToken)).thenReturn(true);
    when(controllerService.processIpUpdate(host, setting.getIpv4(), null, request))
        .thenReturn(ResponseEntity.ok().build());
    ResponseEntity<Void> responseEntity =
        apiController.updateHost(host, validToken, setting.getIpv4(), null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(controllerService, times(1)).processIpUpdate(host, setting.getIpv4(), null, request);
  }

  @Test
  void testSuccess_guessRemoteIp() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testSuccess_guessRemoteIp: {}", host);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, validToken)).thenReturn(true);
    when(controllerService.processIpUpdate(host, null, null, request))
        .thenReturn(ResponseEntity.ok().build());
    ResponseEntity<Void> responseEntity =
        apiController.updateHost(host, validToken, null, null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(controllerService, times(1)).processIpUpdate(host, null, null, request);
  }

  @Test
  void testWithInvalidHost() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testWithInvalidHost: {}", host);
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, validToken)).thenThrow(EmptyResultDataAccessException.class);

    try {
      apiController.updateHost(host, validToken, InetAddress.getByName(ipStr), null, request);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    verify(provider, never()).info(host);
    verify(provider, never()).processUpdate(host, setting);
    verify(updateLogService, never()).log(eq(host), any(IpSetting.class), any(UpdateLog.Status.class));
  }

  @Test
  void testWithInvalidToken() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testWithInvalidToken: {}", host);
    String invalidToken = "invalid_token";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, invalidToken)).thenReturn(false);

    try {
      apiController.updateHost(host, invalidToken, setting.getIpv4(), null, request);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }

    verify(provider, never()).info(host);
    verify(provider, never()).processUpdate(host, setting);
    verify(updateLogService, never()).log(eq(host), any(IpSetting.class), any(UpdateLog.Status.class));
  }

  @Test
  void testWithProviderException() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testWithProviderException: {}", host);
    String apiToken = "valid_token";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, apiToken)).thenReturn(true);
    doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
        .when(controllerService).processIpUpdate(host, setting.getIpv4(), null, request);

    try {
      apiController.updateHost(host, apiToken, setting.getIpv4(), null, request);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
    }

    verify(controllerService, times(1)).processIpUpdate(host, setting.getIpv4(), null, request);
  }

  @Test
  void routerUpdateHost_successfulUpdate() throws Exception {
    String host = "valid-host";
    InetAddress ipv4 = InetAddress.getByName("192.168.1.1");
    InetAddress ipv6 = InetAddress.getByName("::1");
    HttpServletRequest req = mock(HttpServletRequest.class);
    UserDetails userDetails = mock(UserDetails.class);

    when(userDetails.getUsername()).thenReturn(host);
    when(hostZoneService.getHost(host)).thenReturn(Optional.of(mock(HostEnriched.class)));
    when(dynamicSecurityChainManager.isHostRegistered(host)).thenReturn(true);
    when(controllerService.processIpUpdate(host, ipv4, ipv6, req)).thenReturn(ResponseEntity.ok().build());

    ResponseEntity<Void> response = routerController.routerUpdateHost(userDetails, host, ipv4, ipv6, req);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(controllerService).processIpUpdate(host, ipv4, ipv6, req);
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
    verify(provider, never()).processUpdate(anyString(), any(IpSetting.class));
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
    verify(provider, never()).processUpdate(anyString(), any(IpSetting.class));
  }
}
