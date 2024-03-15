package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.net.UnknownHostException;

import codes.thischwa.dyndrest.service.UpdateLogService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public class UpdateLogServiceTest extends AbstractIntegrationTest {

  @Autowired private UpdateLogService updateLogService;

  @BeforeAll
  void init() {
    //initUpdateLogDatabase();
  }

  @Test
  void testCount() {
    assertEquals(43, updateLogService.count());
  }

  @Test
  void testGetPage() {
    Page<FullUpdateLog> page = updateLogService.getPage(0);
    assertNotNull(page);
    assertEquals(43, page.getTotalElements());
    assertEquals(4, page.getSize());
    assertEquals(11, page.getTotalPages());
    assertTrue(page.isFirst());
    assertEquals(0, page.getNumber());

    FullUpdateLog log = page.getContent().get(0);
    assertEquals(43, log.getId());
    assertEquals(2, log.getHostId());
    assertEquals("test0.dynhost0.info", log.getHost());
    assertEquals("198.0.2.254", log.getIpv4());
    assertEquals(UpdateLog.Status.failed, log.getStatus());
    assertEquals(START_DATETIME.withHour(20).withMinute(30), log.getChangedUpdate());
    assertEquals(START_DATETIME.withHour(20).withMinute(30), log.getChanged());

    log = page.getContent().get(3);
    assertEquals(40, log.getId());
    assertEquals(2, log.getHostId());
    assertEquals("test0.dynhost0.info", log.getHost());
    assertEquals("198.0.2.18", log.getIpv4());
    assertEquals(UpdateLog.Status.virgin, log.getStatus());
    assertNull(log.getChangedUpdate());
    assertEquals(START_DATETIME.withHour(20).withMinute(0), log.getChanged());

    page = updateLogService.getPage(10);
    assertTrue(page.isLast());
    assertEquals(4, page.getSize());
    assertEquals(3, page.getNumberOfElements());
  }
}
