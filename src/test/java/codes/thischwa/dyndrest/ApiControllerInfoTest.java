package codes.thischwa.dyndrest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;

import java.net.InetAddress;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class ApiControllerInfoTest extends AbstractApiControllerTest {

  @Test
  void testSuccess() throws Exception {
    String host = "test1.mydns.com";
    String apitoken = "valid_token";
    IpSetting setting = new IpSetting("192.168.1.1");

    when(hostZoneService.validate(host, apitoken)).thenReturn(true);
    when(provider.info(host)).thenReturn(setting);

    ResponseEntity<IpSetting> result = apiController.info(host, apitoken);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(setting, result.getBody());
    verify(hostZoneService).validate(host, apitoken);
    verify(provider).info(host);
  }

  @Test
  void testWithInvalidHost() throws Exception {
    String host = "invalid.mydns.com";
    String apitoken = "valid_token";

    when(hostZoneService.validate(host, apitoken)).thenThrow(EmptyResultDataAccessException.class);

    try {
      apiController.info(host, apitoken);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
    verify(hostZoneService).validate(host, apitoken);
  }

  @Test
  void testWithInvalidApitoken() throws Exception {
    String host = "test2.mydns.com";
    String apitoken = "invalid_token";

    when(hostZoneService.validate(host, apitoken)).thenReturn(false);

    try {
      apiController.info(host, apitoken);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }
    verify(hostZoneService).validate(host, apitoken);
  }

  @Test
  void testWithProviderException() throws Exception {
    String host = "test3.mydns.com";
    String apitoken = "valid_token";

    when(hostZoneService.validate(host, apitoken)).thenReturn(true);
    when(provider.info(host)).thenThrow(new ProviderException("Unknown Exception"));

    ResponseEntity<IpSetting> result = apiController.info(host, apitoken);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
  }
}
