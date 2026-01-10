package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.UpdateLogEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.net.UnknownHostException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UpdateLogServiceTest extends AbstractIntegrationTest {

  @Autowired private UpdateLogService updateLogService;

  @Order(1)
  @Test
  void testGetPage() {
    Page<UpdateLogEnriched> page = updateLogService.getPage(0);
    assertNotNull(page);
    assertEquals(42, page.getTotalElements());
    assertEquals(4, page.getSize());
    assertEquals(11, page.getTotalPages());
    assertTrue(page.isFirst());
    assertEquals(0, page.getNumber());

    UpdateLogEnriched log = page.getContent().get(0);
    assertEquals(42, log.getId());
    assertEquals(h2z1ID, log.getHostId());
    assertEquals("test0.dynhost0.info", log.getHost());
    assertEquals("198.0.2.20", log.getIpv4());
    assertEquals(UpdateLog.Status.success, log.getStatus());
    assertEquals(START_DATETIME.withHour(20).withMinute(20), log.getChangedUpdate());
    assertEquals(START_DATETIME.withHour(20).withMinute(20), log.getChanged());

    log = page.getContent().get(3);
    assertEquals(39, log.getId());
    assertEquals(h2z1ID, log.getHostId());
    assertEquals("test0.dynhost0.info", log.getHost());
    assertEquals("198.0.2.17", log.getIpv4());
    assertEquals(UpdateLog.Status.success, log.getStatus());
    assertEquals(START_DATETIME.withHour(19).withMinute(50), log.getChangedUpdate());
    assertEquals(START_DATETIME.withHour(19).withMinute(50), log.getChanged());

    page = updateLogService.getPage(10);
    assertTrue(page.isLast());
    assertEquals(4, page.getSize());
    assertEquals(2, page.getNumberOfElements());

    log = page.getContent().get(1);
    assertEquals(1, log.getId());
    assertEquals(h1z1ID, log.getHostId());
    assertEquals("my0.dynhost0.info", log.getHost());
    assertEquals("198.0.1.0", log.getIpv4());
    assertEquals(UpdateLog.Status.failed, log.getStatus());
    assertNull(log.getChangedUpdate());
    assertEquals(START_DATETIME.withHour(13).withMinute(30), log.getChanged());
  }

  @Order(2)
  @Test
  void testLog_unknown() {
    assertThrows(
            IllegalArgumentException.class,
            () ->
                    updateLogService.log(
                            "unknown.dynhost0.info", new IpSetting("129.0.0.3"), UpdateLog.Status.success));
  }

  @Order(3)
  @Test
  void testLog() throws UnknownHostException {
    assertEquals(42, updateLogService.count());
    updateLogService.log("my0.dynhost0.info", new IpSetting("129.0.0.3"), UpdateLog.Status.success);
    assertEquals(43, updateLogService.count());
  }
}
