package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.IpSetting;
import org.domainrobot.sdk.client.JsonUtils;
import org.domainrobot.sdk.models.generated.JsonResponseDataZone;
import org.domainrobot.sdk.models.generated.ResourceRecord;
import org.domainrobot.sdk.models.generated.Zone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ZoneClientWrapperTest extends GenericIntegrationTest {

	private static final int rrCount = 5;

	@Autowired
	private DomainRobotConfig domainRobotConfig;

	private Zone zone;

	private ZoneClientWrapper zcw;

	@BeforeEach
	void setUp() throws Exception {
		JsonResponseDataZone response = JsonUtils.deserialize(
				Objects.requireNonNull(this.getClass().getResourceAsStream("zone-info.json")).readAllBytes(), JsonResponseDataZone.class);
		zone = response.getData().get(0);

		zcw = domainRobotConfig.buildZoneClientWrapper();
	}

	@Test
	final void testUpdateIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub", new IpSetting("128.0.0.1"));
		// AAAA is removed
		assertEquals(rrCount - 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ZoneClientWrapper.ResouceRecordTypeIP.A);
		assertNotNull(rr);
		assertEquals("128.0.0.1", rr.getValue());
	}

	@Test
	final void testUpdateIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub", new IpSetting("2a03:4000:41:32::20"));
		// A is removed
		assertEquals(rrCount - 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ZoneClientWrapper.ResouceRecordTypeIP.AAAA);
		assertNotNull(rr);
		assertEquals("2a03:4000:41:32:0:0:0:20", rr.getValue());
	}

	@Test
	final void testAddIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub1", new IpSetting("128.0.0.1"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub1", ZoneClientWrapper.ResouceRecordTypeIP.A);
		assertNotNull(rr);
		assertEquals("128.0.0.1", rr.getValue());
	}

	@Test
	final void testAddIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub1", new IpSetting("2a03:4000:41:32::20"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub1", ZoneClientWrapper.ResouceRecordTypeIP.AAAA);
		assertNotNull(rr);
		assertEquals("2a03:4000:41:32:0:0:0:20", rr.getValue());
	}

	@Test
	final void testRemoveIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub2", new IpSetting("128.0.0.2"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		zcw.removeIPv4(zone, "sub2");
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.removeIPv4(zone, "unknownsub");
		assertEquals(rrCount, zone.getResourceRecords().size());
	}

	@Test
	final void testRemoveIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub2", new IpSetting("2a03:4000:41:32::20"));
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
		assertFalse(zcw.hasIPsChanged(zone, "sub", new IpSetting("85.209.51.215","2a03:4000:41:32::10")));
		assertTrue(zcw.hasIPsChanged(zone, "unknownsub", new IpSetting("85.209.51.216","2a03:4000:41:32::10")));

		assertTrue(zcw.hasIPsChanged(zone, "sub", new IpSetting("85.209.51.216","2a03:4000:41:32::10")));
		assertTrue(zcw.hasIPsChanged(zone, "sub", new IpSetting("85.209.51.215", "2a03:4000:41:32::11")));

		assertTrue(zcw.hasIPsChanged(zone, "sub", new IpSetting("2a03:4000:41:32::10")));
		assertTrue(zcw.hasIPsChanged(zone, "sub", new IpSetting("85.209.51.215")));

		assertFalse(zcw.hasIPsChanged(zone, "sub", new IpSetting()));
	}
}
