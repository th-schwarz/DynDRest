package codes.thischwa.dyndrest.provider.impl.domainrobot;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.impl.domainrobot.ZoneClientWrapper.ResouceRecordTypeIp;
import java.util.Objects;
import org.domainrobot.sdk.client.JsonUtils;
import org.domainrobot.sdk.models.generated.JsonResponseDataZone;
import org.domainrobot.sdk.models.generated.ResourceRecord;
import org.domainrobot.sdk.models.generated.Zone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ZoneClientWrapperTest extends AbstractIntegrationTest {

	private static int rrCount = 5;

	@Autowired
	private DomainRobotConfigurator domainRobotConfigurator;

	private Zone zone;

	private ZoneClientWrapper zcw;

	@BeforeEach
	void setUp() throws Exception {
		JsonResponseDataZone response = JsonUtils.deserialize(
				Objects.requireNonNull(this.getClass().getResourceAsStream("zone-info.json")).readAllBytes(), JsonResponseDataZone.class);
		zone = response.getData().get(0);

		zcw = domainRobotConfigurator.buildZoneClientWrapper();
	}

	@Test
	void testUpdateIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub", new IpSetting("128.0.0.1"));
		// AAAA is removed
		assertEquals(rrCount - 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ResouceRecordTypeIp.A);
		assertNotNull(rr);
		assertEquals("128.0.0.1", rr.getValue());
	}

	@Test
	void testUpdateIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub", new IpSetting("2a03:4000:41:32::20"));
		// A is removed
		assertEquals(rrCount - 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ResouceRecordTypeIp.AAAA);
		assertNotNull(rr);
		assertEquals("2a03:4000:41:32:0:0:0:20", rr.getValue());
	}

	@Test
	void testAddIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub1", new IpSetting("128.0.0.1"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub1", ResouceRecordTypeIp.A);
		assertNotNull(rr);
		assertEquals("128.0.0.1", rr.getValue());
	}

	@Test
	void testAddIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub1", new IpSetting("2a03:4000:41:32::20"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub1", ResouceRecordTypeIp.AAAA);
		assertNotNull(rr);
		assertEquals("2a03:4000:41:32:0:0:0:20", rr.getValue());
	}

	@Test
	void testRemoveIPv4() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub2", new IpSetting("128.0.0.2"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		zcw.removeIpv4(zone, "sub2");
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.removeIpv4(zone, "unknownsub");
		assertEquals(rrCount, zone.getResourceRecords().size());
	}

	@Test
	void testRemoveIPv6() throws Exception {
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.process(zone, "sub2", new IpSetting("2a03:4000:41:32::20"));
		assertEquals(rrCount + 1, zone.getResourceRecords().size());
		zcw.removeIp6(zone, "sub2");
		assertEquals(rrCount, zone.getResourceRecords().size());
		zcw.removeIp6(zone, "unknownsub");
		assertEquals(rrCount, zone.getResourceRecords().size());
	}

	@Test
	void testSearch() {
		ResourceRecord rr = zcw.searchResourceRecord(zone, "sub", ResouceRecordTypeIp.A);
        assert rr != null;
        assertEquals("85.209.51.215", rr.getValue());
	}

	@Test
	void testDeriveZone() {
		assertEquals("example.com", zcw.deriveZone("sub.example.com"));
	}

	@Test
	void testDeriveZone_fail() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> zcw.deriveZone("example.com"));
	}

	@Test
	void testIPsHasChanged() throws Exception {
		assertFalse(zcw.hasIpsChanged(zone, "sub", new IpSetting("85.209.51.215","2a03:4000:41:32::10")));
		assertTrue(zcw.hasIpsChanged(zone, "unknownsub", new IpSetting("85.209.51.216","2a03:4000:41:32::10")));

		assertTrue(zcw.hasIpsChanged(zone, "sub", new IpSetting("85.209.51.216","2a03:4000:41:32::10")));
		assertTrue(zcw.hasIpsChanged(zone, "sub", new IpSetting("85.209.51.215", "2a03:4000:41:32::11")));

		assertTrue(zcw.hasIpsChanged(zone, "sub", new IpSetting("2a03:4000:41:32::10")));
		assertTrue(zcw.hasIpsChanged(zone, "sub", new IpSetting("85.209.51.215")));

		assertFalse(zcw.hasIpsChanged(zone, "sub", new IpSetting()));
	}

	@Test
	void testHasSubTld() {
		assertTrue(zcw.hasSubTld(zone, "sub"));
		assertFalse(zcw.hasSubTld(zone, "unknown"));
	}
}
