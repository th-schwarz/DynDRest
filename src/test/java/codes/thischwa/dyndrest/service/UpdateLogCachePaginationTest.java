package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.UpdateLogPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateLogCachePaginationTest extends GenericIntegrationTest {

	@Autowired private UpdateLogCache cache;

	@Test
	final void testPageFirst() {
		UpdateLogPage lp = cache.getResponsePage(1, null);
		assertEquals(1, lp.getPage());
		assertEquals(4, lp.getPageSize());
		assertEquals(10, lp.getTotalPage());
		assertEquals(38, lp.getTotal());
		assertEquals(4, lp.getItems().size());
		assertEquals("2022-02-17 03:39:37.606", lp.getItems().get(0).dateTime());
		assertEquals("master.mydyndns.com", lp.getItems().get(0).host());
		assertEquals("2022-02-15 03:11:00.224", lp.getItems().get(3).dateTime());
		assertEquals("ursa.mydyndns.com", lp.getItems().get(3).host());
	}

	@Test
	final void testNullPageFirst() {
		UpdateLogPage lp = cache.getResponsePage(null, null);
		assertEquals(1, lp.getPage());
		assertEquals(4, lp.getPageSize());
		assertEquals(10, lp.getTotalPage());
		assertEquals(38, lp.getTotal());
		assertEquals(4, lp.getItems().size());
		assertEquals("2022-02-17 03:39:37.606", lp.getItems().get(0).dateTime());
		assertEquals("master.mydyndns.com", lp.getItems().get(0).host());
		assertEquals("2022-02-15 03:11:00.224", lp.getItems().get(3).dateTime());
		assertEquals("ursa.mydyndns.com", lp.getItems().get(3).host());
	}

	@Test
	final void testPageFirstSearch() {
		UpdateLogPage lp = cache.getResponsePage(1, "master");
		assertEquals(1, lp.getPage());
		assertEquals(4, lp.getPageSize());
		assertEquals(10, lp.getTotalPage());
		assertEquals(22, lp.getTotal());
		assertEquals(4, lp.getItems().size());
		assertEquals("2022-02-17 03:39:37.606", lp.getItems().get(0).dateTime());
		assertEquals("master.mydyndns.com", lp.getItems().get(0).host());
		assertEquals("2022-02-13 03:44:13.713", lp.getItems().get(3).dateTime());
		assertEquals("master.mydyndns.com", lp.getItems().get(3).host());
	}

	@Test
	final void testPageSecond() {
		UpdateLogPage lp = cache.getResponsePage(2, "");
		assertEquals(2, lp.getPage());
		assertEquals(4, lp.getPageSize());
		assertEquals(10, lp.getTotalPage());
		assertEquals(38, lp.getTotal());
		assertEquals(4, lp.getItems().size());
		assertEquals("2022-02-14 03:43:19.560", lp.getItems().get(0).dateTime());
		assertEquals("2022-02-13 03:13:16.931", lp.getItems().get(3).dateTime());
	}

	@Test
	final void testPageLast() {
		UpdateLogPage lp = cache.getResponsePage(10, null);
		assertEquals(10, lp.getPage());
		assertEquals(4, lp.getPageSize());
		assertEquals(10, lp.getTotalPage());
		assertEquals(38, lp.getTotal());
		assertEquals(2, lp.getItems().size());
		assertEquals("2022-02-01 03:33:17.476", lp.getItems().get(0).dateTime());
		assertEquals("2022-02-01 03:28:11.497", lp.getItems().get(1).dateTime());
	}
}
