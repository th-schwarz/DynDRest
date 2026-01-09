package codes.thischwa.dyndrest.provider.impl.cloudflare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import codes.thischwa.cf.CfDnsClient;
import codes.thischwa.cf.CloudflareApiException;
import codes.thischwa.cf.model.RecordEntity;
import codes.thischwa.cf.model.RecordType;
import codes.thischwa.cf.model.ZoneEntity;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.ZoneUpdateOrderService;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CloudflareProviderMockTest {

	private CloudflareProvider provider;
	private CfDnsClient cfDnsClient;
	private HostZoneService hostZoneService;
	private ZoneEntity zoneEntity;

	@BeforeEach
	void setUp() {
		AppConfig appConfig = mock(AppConfig.class);
		CloudflareConfig config = mock(CloudflareConfig.class);
		when(config.defaultTtl()).thenReturn(300);
		when(config.email()).thenReturn("test@example.com");
		when(config.apiKey()).thenReturn("test-api-key");

		hostZoneService = mock(HostZoneService.class);
		ZoneUpdateOrderService zoneUpdateOrderService = mock(ZoneUpdateOrderService.class);
		DynamicSecurityChainManager securityChainManager = mock(DynamicSecurityChainManager.class);

		provider = new CloudflareProvider(appConfig, config, hostZoneService, zoneUpdateOrderService, securityChainManager);
		cfDnsClient = mock(CfDnsClient.class);
		provider.cfDnsClient = cfDnsClient;

		zoneEntity = new ZoneEntity();
		zoneEntity.setName("example.com");
		zoneEntity.setId("zone-id-123");
	}

	@Test
	void testCreateUpdateDeleteRecord() throws CloudflareApiException, ProviderException, UnknownHostException {
		String host = "test.example.com";
		String sld = "test";
		Inet4Address ipv4 = (Inet4Address) Inet4Address.getByName("192.0.2.1");
		Inet4Address ipv4Updated = (Inet4Address) Inet4Address.getByName("192.0.2.2");

		HostEnriched hostEnriched = mock(HostEnriched.class);
		when(hostEnriched.getZone()).thenReturn("example.com");
		when(hostZoneService.getHost(host)).thenReturn(Optional.of(hostEnriched));
		when(cfDnsClient.zoneGet("example.com")).thenReturn(zoneEntity);

		// CREATE: No existing records, create new A record
		when(cfDnsClient.recordList(eq(zoneEntity), eq(sld), eq(RecordType.A)))
				.thenReturn(Collections.emptyList());
		when(cfDnsClient.recordList(eq(zoneEntity), eq(sld), eq(RecordType.AAAA)))
				.thenReturn(Collections.emptyList());

		IpSetting createIpSetting = new IpSetting();
		createIpSetting.setIpv4(ipv4);
		provider.update(host, createIpSetting);

		ArgumentCaptor<RecordEntity> createCaptor = ArgumentCaptor.forClass(RecordEntity.class);
		verify(cfDnsClient, times(1)).recordCreate(eq(zoneEntity), createCaptor.capture());
		RecordEntity createdRecord = createCaptor.getValue();
		assertEquals(sld, createdRecord.getName());
		assertEquals(RecordType.A.getType(), createdRecord.getType());
		assertEquals("192.0.2.1", createdRecord.getContent());
		assertEquals(300, createdRecord.getTtl());

		// UPDATE: Existing record with different IP, update it
		RecordEntity existingRecord = new RecordEntity();
		existingRecord.setId("record-id-123");
		existingRecord.setName(sld);
		existingRecord.setType(RecordType.A.getType());
		existingRecord.setContent("192.0.2.1");
		existingRecord.setTtl(300);

		when(cfDnsClient.recordList(eq(zoneEntity), eq(sld), eq(RecordType.A)))
				.thenReturn(Collections.singletonList(existingRecord));

		IpSetting updateIpSetting = new IpSetting();
		updateIpSetting.setIpv4(ipv4Updated);
		provider.update(host, updateIpSetting);

		ArgumentCaptor<RecordEntity> updateCaptor = ArgumentCaptor.forClass(RecordEntity.class);
		verify(cfDnsClient, times(1)).recordUpdate(eq(zoneEntity), updateCaptor.capture());
		RecordEntity updatedRecord = updateCaptor.getValue();
		assertEquals("192.0.2.2", updatedRecord.getContent());

		// DELETE: Set IP to null, delete the record
		when(cfDnsClient.recordList(eq(zoneEntity), eq(sld), eq(RecordType.A)))
				.thenReturn(Collections.singletonList(existingRecord));

		IpSetting deleteIpSetting = new IpSetting();
		provider.update(host, deleteIpSetting);

		verify(cfDnsClient, times(1)).recordDelete(eq(zoneEntity), eq(existingRecord));
	}
}