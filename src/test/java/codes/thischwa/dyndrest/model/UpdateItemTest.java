package codes.thischwa.dyndrest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UpdateItemTest {

  @Test
  final void testItem() {
    UpdateItem item = new UpdateItem("testDateTime", "testHost", null, null);
    assertEquals(
        "UpdateItem[dateTime=testDateTime, host=testHost, ipv4=n/a, ipv6=n/a]", item.toString());
    item = new UpdateItem("testDateTime", "testHost", "testipv4", "testipv6");
    assertEquals(
        "UpdateItem[dateTime=testDateTime, host=testHost, ipv4=testipv4, ipv6=testipv6]",
        item.toString());
  }

  @Test
  final void testCompare() {
    UpdateItem item1 = new UpdateItem("testDateTime1", "testHost", "testipv4", "testipv6");
    UpdateItem item2 = new UpdateItem("testDateTime2", "testHost", "testipv4", "testipv6");
    assertEquals(-1, item1.compareTo(item2));
  }
}
