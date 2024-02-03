package codes.thischwa.dyndrest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.UpdateItem;
import codes.thischwa.dyndrest.model.UpdateLogPage;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UpdateLogCacheTest extends AbstractIntegrationTest {

  private final int startCnt = 38;

  private final Pattern logEntryPattern =
      Pattern.compile("(.*)\\s+-\\s+([a-zA-Z.-]*)\\s+(\\S*)\\s+(\\S*)");

  @Autowired private UpdateLogCache cache;

  @Test
  final void testCache() {
    assertTrue(cache.isEnabled());
    assertEquals(startCnt, cache.size());
    assertEquals(startCnt, cache.getAllItems().size());
  }

  @Test
  final void testCompareEqualsHash() {
    UpdateItem item1 = cache.getAllItems().get(0);
    UpdateItem item2 = cache.getAllItems().get(1);

    assertEquals(item1, item1);
    assertTrue(item1.compareTo(item2) < 0);
    assertTrue(item2.compareTo(item1) > 0);

    assertNotEquals(item1.hashCode(), item2.hashCode());

    assertNotEquals(item1, item2);
    assertNotEquals("string", item1);
  }

  @Test
  final void testAddLogEntry() {
    assertEquals(startCnt, cache.size());
    cache.addLogItem("my.dyndns.com", "91.0.0.1", null);
    assertEquals(startCnt + 1, cache.size());

    UpdateItem item = cache.getAllItems().remove(startCnt);
    assertEquals("my.dyndns.com", item.host());
    assertEquals("91.0.0.1", item.ipv4());
    assertEquals("n/a", item.ipv6());

    cache.addLogItem("my.dyndns.com", "91.0.0.1", "2003:cc:2fff:1131:2e91:abff:febf:d839");
    item = cache.getAllItems().remove(startCnt);
    assertEquals("my.dyndns.com", item.host());
    assertEquals("91.0.0.1", item.ipv4());
    assertEquals("2003:cc:2fff:1131:2e91:abff:febf:d839", item.ipv6());
  }

  @Test
  final void testParseLogEntry() {
    assertNull(cache.parseLogEntry(null, logEntryPattern));
    assertNull(cache.parseLogEntry("abc", logEntryPattern));

    UpdateItem item =
        cache.parseLogEntry(
            "2022-02-23 19:51:19.924 -   test.mein-virtuelles-blech.de        127.1.2.27  2a03:4000:41:32::2",
            logEntryPattern);
    assertEquals("2022-02-23 19:51:19.924", item.dateTime());
    assertEquals("test.mein-virtuelles-blech.de", item.host());
    assertEquals("127.1.2.27", item.ipv4());
    assertEquals("2a03:4000:41:32::2", item.ipv6());
  }

  @Test
  final void testResponseAll() {
    UpdateLogPage lp = cache.getResponseAll();
    assertEquals(startCnt, lp.getTotal());
    assertEquals(startCnt, lp.getItems().size());
    assertEquals(0, lp.getTotalPage());
    assertEquals(0, lp.getPage());
    assertEquals(4, lp.getPageSize());
  }
}
