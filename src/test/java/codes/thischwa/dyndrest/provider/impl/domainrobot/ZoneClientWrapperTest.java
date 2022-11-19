package codes.thischwa.dyndrest.provider.impl.domainrobot;

import org.domainrobot.sdk.client.JsonUtils;
import org.domainrobot.sdk.models.generated.JsonResponseDataZone;
import org.domainrobot.sdk.models.generated.ResourceRecord;
import org.domainrobot.sdk.models.generated.Zone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ZoneClientWrapperTest {

	private static final int rrCount = 5;
	private final ZoneClientWrapper zcw = new ZoneClientWrapper();
	private Zone zone;

	@BeforeEach
	void setUp() throws Exception {
		JsonResponseDataZone response = JsonUtils.deserialize(
				Objects.requireNonNull(this.getClass().getResourceAsStream("zone-info.json")).readAllBytes(), JsonResponseDataZone.class);
		zone = response.getData().get(0);
	}

	@Test
	final void testUpdateIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.addOrUpdateIPv4(zone, "sub", (Inet4Address) InetAddress.getByName("128.0.0.1"));
		assertEquals(rrCount, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ZoneClientWrapper.ResouceRecordTypeIP.A);
		assertNotNull(rr);
		assertEquals("128.0.0.1", rr.getValue());
	}

	@Test
	final void testUpdateIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.addOrUpdateIPv6(zone, "sub", (Inet6Address) InetAddress.getByName("2a03:4000:41:32::20"));
		assertEquals(rrCount, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ZoneClientWrapper.ResouceRecordTypeIP.AAAA);
		assertNotNull(rr);
		assertEquals("2a03:4000:41:32:0:0:0:20", rr.getValue());
	}

	@Test
	final void testAddIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.addOrUpdateIPv4(zone, "sub1", (Inet4Address) InetAddress.getByName("128.0.0.1"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub1", ZoneClientWrapper.ResouceRecordTypeIP.A);
		assertNotNull(rr);
		assertEquals("128.0.0.1", rr.getValue());
	}

	@Test
	final void testAddIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.addOrUpdateIPv6(zone, "sub1", getV6("2a03:4000:41:32::20"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub1", ZoneClientWrapper.ResouceRecordTypeIP.AAAA);
		assertNotNull(rr);
		assertEquals("2a03:4000:41:32:0:0:0:20", rr.getValue());
	}

	@Test
	final void testRemoveIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.addOrUpdateIPv4(zone, "sub2", getV4("128.0.0.2"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		zcw.removeIPv4(zone, "sub2");
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.removeIPv4(zone, "unknownsub");
		assertEquals(rrCount, zone.getResourceRecords().size());
	}

	@Test
	final void testRemoveIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.addOrUpdateIPv6(zone, "sub2", getV6("2a03:4000:41:32::20"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		zcw.removeIPv6(zone, "sub2");
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.removeIPv6(zone, "unknownsub");
		assertEquals(rrCount, zone.getResourceRecords().size());
	}

	@Test
	final void testSearch() {
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ZoneClientWrapper.ResouceRecordTypeIP.A);
		assertEquals("85.209.51.215", rr.getValue());
	}

	@Test
	final void testDeriveZone() {
		assertEquals("example.com", zcw.deriveZone("sub.example.com"));
	}

	@Test
	final void testDeriveZone_fail() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> zcw.deriveZone("example.com"));
	}

	@Test
	final void testIPsHasChanged() throws Exception {
		assertFalse(zcw.hasIPsChanged(zone, "sub", getV4("85.209.51.215"), getV6("2a03:4000:41:32::10")));
		assertTrue(zcw.hasIPsChanged(zone, "unknownsub", getV4("85.209.51.216"), getV6("2a03:4000:41:32::10")));

		assertTrue(zcw.hasIPsChanged(zone, "sub", getV4("85.209.51.216"), getV6("2a03:4000:41:32::10")));
		assertTrue(zcw.hasIPsChanged(zone, "sub", getV4("85.209.51.215"), getV6("2a03:4000:41:32::11")));

		assertTrue(zcw.hasIPsChanged(zone, "sub", null, getV6("2a03:4000:41:32::10")));
		assertTrue(zcw.hasIPsChanged(zone, "sub", getV4("85.209.51.215"), null));
	}

	@Test
	final void testHasIP() {
		assertFalse(zcw.hasIP(null, null));
	}

	private Inet4Address getV4(String ip) throws UnknownHostException {
		return (Inet4Address) InetAddress.getByName(ip);
	}

	private Inet6Address getV6(String ip) throws UnknownHostException {
		return (Inet6Address) InetAddress.getByName(ip);
	}
}
