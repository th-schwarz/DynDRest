package codes.thischwa.dyndrest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.provider.ProviderException;
import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
class ApiControllerUpdateTest extends AbstractApiControllerTest {

  private final String ipStr = "192.168.1.1";
  private final String validToken = "valid_token";

  @Test
  void testSuccess() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testSuccess: {}", host);
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(provider.info(host)).thenReturn(new IpSetting("192.168.2.0"));
    when(hostZoneService.validate(host, validToken)).thenReturn(true);
    ResponseEntity<Object> responseEntity =
        apiController.update(host, validToken, InetAddress.getByName(ipStr), null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(provider, times(1)).info(host);
    verify(provider, times(1)).processUpdate(host, setting);
    verify(updateLogService, times(1))
        .log(host, setting, UpdateLog.Status.success);
  }

  @Test
  void testSuccess_guessRemoteIp() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testSuccess_guessRemoteIp: {}", host);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");
    IpSetting remoteIp = new IpSetting(request.getRemoteAddr());

    when(provider.info(host)).thenReturn(new IpSetting("192.168.2.0"));
    when(hostZoneService.validate(host, validToken)).thenReturn(true);
    ResponseEntity<Object> responseEntity =
            apiController.update(host, validToken, null, null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(provider, times(1)).info(host);
    verify(provider, times(1)).processUpdate(host, remoteIp);
    verify(updateLogService, times(1))
        .log(host, remoteIp, UpdateLog.Status.success);
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
      apiController.update(host, validToken, InetAddress.getByName(ipStr), null, request);
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
      apiController.update(host, invalidToken, InetAddress.getByName(ipStr), null, request);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    verify(provider, never()).info(host);
    verify(provider, never()).processUpdate(host, setting);
    verify(updateLogService, never()).log(eq(host), any(IpSetting.class), any(UpdateLog.Status.class));
  }

  @Test
  void testWithProviderException() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testWithProviderException: {}", host);
    String apitoken = "valid_token";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, apitoken)).thenReturn(true);
    when(provider.info(host)).thenThrow(ProviderException.class);

    try {
      apiController.update(host, apitoken, InetAddress.getByName(ipStr), null, request);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
    }

    verify(provider, times(1)).info(host);
    verify(provider, never()).processUpdate(host, setting);
    verify(updateLogService, times(1))
        .log(host, setting, UpdateLog.Status.failed);
  }
}
