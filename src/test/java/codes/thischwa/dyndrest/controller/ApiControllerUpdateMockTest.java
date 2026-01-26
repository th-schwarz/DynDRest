package codes.thischwa.dyndrest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("Integration tests: controller - api-addOrUpdate")
@Slf4j
class ApiControllerUpdateMockTest extends AbstractControllerMockTest {

  private final String ipStr = "192.168.1.1";
  private final String validToken = "valid_token";

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("dyndrest.zone-update-scheduler-enabled", () -> true);
    registry.add("dyndrest.zone-update-scheduler-interval-seconds", () -> 1);
  }

  @BeforeEach
  void resetMocks() {
    reset(provider, hostZoneService, dynamicSecurityChainManager, zoneUpdaterService, updateLogService);
  }

  @Test
  void testSuccess() throws Exception {
    String host = buildHostName("domain.addOrUpdate");
    log.debug("entered #testSuccess: {}", host);
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");
    HostEnriched hostEnriched = mock(HostEnriched.class);

    when(hostZoneService.validate(host, validToken)).thenReturn(true);
    when(hostZoneService.getHost(host)).thenReturn(Optional.of(hostEnriched));
    
    ResponseEntity<Void> responseEntity =
        apiController.updateHost(host, validToken, setting.getIpv4(), null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(zoneUpdaterService, times(1)).addOrUpdateHost(any());
  }

  @Test
  void testSuccess_guessRemoteIp() throws Exception {
    String host = buildHostName("domain.addOrUpdate");
    log.debug("entered #testSuccess_guessRemoteIp: {}", host);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");
    HostEnriched hostEnriched = mock(HostEnriched.class);

    when(hostZoneService.validate(host, validToken)).thenReturn(true);
    when(hostZoneService.getHost(host)).thenReturn(Optional.of(hostEnriched));
    
    ResponseEntity<Void> responseEntity =
        apiController.updateHost(host, validToken, null, null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(zoneUpdaterService, times(1)).addOrUpdateHost(any());
  }

  @Test
  void testWithInvalidHost() throws Exception {
    String host = buildHostName("domain.addOrUpdate");
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
   verify(provider, never()).addOrUpdate(host, setting);
    verify(updateLogService, never()).log(eq(host), any(IpSetting.class), any(UpdateLog.Status.class));
  }

  @Test
  void testWithInvalidToken() throws Exception {
    String host = buildHostName("domain.addOrUpdate");
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
  //  verify(provider, never()).processUpdate(host, setting);
    verify(updateLogService, never()).log(eq(host), any(IpSetting.class), any(UpdateLog.Status.class));
  }

  @Test
  void testWithProviderException() throws Exception {
    String host = buildHostName("domain.addOrUpdate");
    log.debug("entered #testWithProviderException: {}", host);
    String apiToken = "valid_token";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");
    HostEnriched hostEnriched = mock(HostEnriched.class);

    when(hostZoneService.validate(host, apiToken)).thenReturn(true);
    when(hostZoneService.getHost(host)).thenReturn(Optional.of(hostEnriched));
    doThrow(new RuntimeException("Provider error"))
        .when(zoneUpdaterService).addOrUpdateHost(any());

    try {
      apiController.updateHost(host, apiToken, setting.getIpv4(), null, request);
      fail("should fail");
    } catch (RuntimeException e) {
      assertEquals("Provider error", e.getMessage());
    }

    verify(zoneUpdaterService, times(1)).addOrUpdateHost(any());
  }

}
