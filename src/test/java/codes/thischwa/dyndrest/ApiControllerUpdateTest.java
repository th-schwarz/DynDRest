package codes.thischwa.dyndrest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.provider.ProviderException;
import java.net.InetAddress;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class ApiControllerUpdateTest extends AbstractApiControllerTest {

  @Test
  void testSuccess() throws Exception {
    String host = "validupd1.mydns.com";
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
  void testWithInvalidHost() throws Exception {
    String host = "invalidupd2.mydns.com";
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
    verify(updateLogService, never()).log(any(String.class), any(IpSetting.class), any(UpdateLog.Status.class));
  }

  @Test
  @Disabled
  void testWithInvalidToken() throws Exception {
    String host = "validupd3.mydns.com";
    String apitoken = "valid_token";
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
    verify(updateLogService, never()).log(any(String.class), any(IpSetting.class), any(UpdateLog.Status.class));
  }

  @Test
  void testWithProviderException() throws Exception {
    String host = "validupd4.mydns.com";
    String apitoken = "valid_token";
    String ipStr = "192.168.1.1";
    IpSetting setting = new IpSetting(ipStr);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.10");

    when(provider.info(host)).thenThrow(ProviderException.class);
    when(hostZoneService.validate(host, apitoken)).thenReturn(true);

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
