package codes.thischwa.dyndrest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
class ApiControllerInfoTest extends AbstractApiControllerTest {

  @Test
  void testSuccess() throws Exception {
    String host = buildHostName("domain.info");
    log.debug("entered #testSuccess: {}", host);
    String apiToken = "valid_token";
    IpSetting setting = new IpSetting("192.168.1.1");

    when(hostZoneService.validate(host, apiToken)).thenReturn(true);
    when(provider.info(host)).thenReturn(setting);

    ResponseEntity<IpSetting> result = apiController.fetchHostIpSetting(host, apiToken);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(setting, result.getBody());
    verify(hostZoneService).validate(host, apiToken);
    verify(provider).info(host);
  }

  @Test
  void testWithInvalidHost() throws Exception {
    String host = buildHostName("domain.info");
    log.debug("entered #testWithInvalidHost: {}", host);
    String apiToken = "valid_token";

    when(hostZoneService.validate(host, apiToken)).thenThrow(EmptyResultDataAccessException.class);

    try {
      apiController.fetchHostIpSetting(host, apiToken);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
    verify(hostZoneService).validate(host, apiToken);
  }

  @Test
  void testWithInvalidApitoken() throws Exception {
    String host = buildHostName("domain.info");
    log.debug("entered #testWithInvalidApitoken: {}", host);
    String apiToken = "invalid_token";

    when(hostZoneService.validate(host, apiToken)).thenReturn(false);

    try {
      apiController.fetchHostIpSetting(host, apiToken);
      fail("should fail");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }
    verify(hostZoneService).validate(host, apiToken);
  }

  @Test
  void testWithProviderException() throws Exception {
    String host = buildHostName("domain.info");
    log.debug("entered #testWithProviderException: {}", host);
    String apiToken = "valid_token";

    when(hostZoneService.validate(host, apiToken)).thenReturn(true);
    when(provider.info(host)).thenThrow(new ProviderException("Unknown Exception"));

    ResponseEntity<IpSetting> result = apiController.fetchHostIpSetting(host, apiToken);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
  }
}
