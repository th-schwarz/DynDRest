package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

class UpdateLogServiceTest extends AbstractIntegrationTest {

  @Autowired private UpdateLogService updateLogService;

  @Test
  void testLog() throws UnknownHostException {
    assertEquals(42, updateLogService.count());
    updateLogService.log("my0.dynhost0.info", new IpSetting("129.0.0.3"), UpdateLog.Status.success);
    assertEquals(43, updateLogService.count());
  }

  @Test
  void testLog_unknown() throws Exception {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            updateLogService.log(
                "unknown.dynhost0.info", new IpSetting("129.0.0.3"), UpdateLog.Status.success));
  }

  @Test
  void testGetPage() {
    Page<FullUpdateLog> page = updateLogService.getPage(0);
    assertNotNull(page);
    assertEquals(42, page.getTotalElements());
    assertEquals(4, page.getSize());
    assertEquals(11, page.getTotalPages());
    assertTrue(page.isFirst());
    assertEquals(0, page.getNumber());

    FullUpdateLog log = page.getContent().get(0);
    assertEquals(42, log.getId());
    assertEquals(2, log.getHostId());
    assertEquals("test0.dynhost0.info", log.getHost());
    assertEquals("198.0.2.20", log.getIpv4());
    assertEquals(UpdateLog.Status.success, log.getStatus());
    assertEquals(START_DATETIME.withHour(20).withMinute(20), log.getChangedUpdate());
    assertEquals(START_DATETIME.withHour(20).withMinute(20), log.getChanged());

    log = page.getContent().get(3);
    assertEquals(39, log.getId());
    assertEquals(2, log.getHostId());
    assertEquals("test0.dynhost0.info", log.getHost());
    assertEquals("198.0.2.17", log.getIpv4());
    assertEquals(UpdateLog.Status.success, log.getStatus());
    assertEquals(START_DATETIME.withHour(19).withMinute(50), log.getChangedUpdate());
    assertEquals(START_DATETIME.withHour(19).withMinute(50), log.getChanged());

    page = updateLogService.getPage(10);
    assertTrue(page.isLast());
    assertEquals(4, page.getSize());
    assertEquals(2, page.getNumberOfElements());
  }
}
