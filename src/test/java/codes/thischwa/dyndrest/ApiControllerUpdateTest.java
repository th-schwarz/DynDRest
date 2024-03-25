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

  @Test
  void testSuccess() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testSuccess: {}", host);
    String apitoken = "valid_token";
    String ipStr = "192.168.1.1";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(provider.info(host)).thenReturn(new IpSetting("192.168.2.0"));
    when(hostZoneService.validate(host, apitoken)).thenReturn(true);
    ResponseEntity<Object> responseEntity =
        apiController.update(host, apitoken, InetAddress.getByName(ipStr), null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(provider, times(1)).info(host);
    verify(provider, times(1)).processUpdate(host, setting);
    verify(updateLogService, times(1))
        .log(eq(host), eq(setting), eq(UpdateLog.Status.success));
  }

  @Test
  void testSuccess_guessRemoteIp() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testSuccess: {}", host);
    String apitoken = "valid_token";
    String ipStr = "192.168.1.1";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");
    IpSetting remoteIp = new IpSetting(request.getRemoteAddr());

    when(provider.info(host)).thenReturn(new IpSetting("192.168.2.0"));
    when(hostZoneService.validate(host, apitoken)).thenReturn(true);
    ResponseEntity<Object> responseEntity =
            apiController.update(host, apitoken, null, null, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(provider, times(1)).info(host);
    verify(provider, times(1)).processUpdate(host, remoteIp);
    verify(updateLogService, times(1))
        .log(eq(host), eq(remoteIp), eq(UpdateLog.Status.success));
  }

  @Test
  void testWithInvalidHost() throws Exception {
    String host = buildHostName("domain.update");
    log.debug("entered #testWithInvalidToken: {}", host);
    String apitoken = "valid_token";
    String ipStr = "192.168.1.1";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, apitoken)).thenThrow(EmptyResultDataAccessException.class);

    try {
      apiController.update(host, apitoken, InetAddress.getByName(ipStr), null, request);
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
    String apitoken = "invalid_token";
    String ipStr = "192.168.1.1";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(hostZoneService.validate(host, apitoken)).thenReturn(false);

    try {
      apiController.update(host, apitoken, InetAddress.getByName(ipStr), null, request);
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
    String ipStr = "192.168.1.1";
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
        .log(eq(host), eq(setting), eq(UpdateLog.Status.failed));
  }
}
