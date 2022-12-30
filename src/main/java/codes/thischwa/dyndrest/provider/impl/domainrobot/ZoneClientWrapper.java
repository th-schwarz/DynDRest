package codes.thischwa.dyndrest.provider.impl.domainrobot;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import org.domainrobot.sdk.client.clients.ZoneClient;
import org.domainrobot.sdk.models.DomainrobotApiException;
import org.domainrobot.sdk.models.generated.ResourceRecord;
import org.domainrobot.sdk.models.generated.Zone;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 *  Encapsulate the {@link ZoneClient} and adds same useful util methods.
 */
class ZoneClientWrapper {

	enum ResouceRecordTypeIP {
		A, AAAA
	}

	private final Map<String, String> customHeaders;

	private final long defaultTtl;

	private final ZoneClient zc;

	ZoneClientWrapper(ZoneClient zc, Map<String, String> customHeaders, long defaultTtl) {
		this.zc = zc;
		this.customHeaders = customHeaders;
		this.defaultTtl = defaultTtl;
	}

	ResourceRecord searchResourceRecord(Zone zone, String name, ResouceRecordTypeIP type) {
		return zone.getResourceRecords().stream().filter(rr -> rr.getType().equals(type.toString()) && rr.getName().equals(name))
				.findFirst().orElse(null);
	}

	String deriveZone(String host) {
		long cnt = host.chars().filter(ch -> ch == '.').count();
		if(cnt < 2)
			throw new IllegalArgumentException("'host' must be a sub domain.");
		return host.substring(host.indexOf(".") + 1);
	}

	boolean hasIPsChanged(Zone zone, String sld, IpSetting ipSetting) {
		if(ipSetting.isNotSet())
			return false;
		ResourceRecord rrv4 = searchResourceRecord(zone, sld, ResouceRecordTypeIP.A);
		ResourceRecord rrv6 = searchResourceRecord(zone, sld, ResouceRecordTypeIP.AAAA);
		boolean ipv4Changed = !hasIPChanged(rrv4, ipSetting.getIpv4());
		boolean ipv6Changed = !hasIPChanged(rrv6, ipSetting.getIpv6());
		return ipv4Changed || ipv6Changed;
	}

	private boolean hasIPChanged(ResourceRecord rr, InetAddress ip) {
		if(rr == null || ip == null)
			return false;
		try {
			InetAddress rrIp = InetAddress.getByName(rr.getValue());
			return rrIp.equals(ip);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Couldn't get Ip from value: " + rr.getValue(), e);
		}
	}

	 void update(Zone zone) throws ProviderException {
		try {
			zc.update(zone, customHeaders);
		} catch (DomainrobotApiException e) {
			throw new ProviderException("API exception", e);
		} catch (Exception e) {
			throw new ProviderException("Unknown exception", e);
		}
	}

	/**
	 * Processes a zone-info for 'zone' and 'primaryNameServer'.
	 *
	 * @param zone              the zone to process the info
	 * @param primaryNameServer the primary NS of the zone
	 * @return the complete zone object from the domainrobot sdk
	 *
	 * @throws ProviderException if an exception happens while processing the zone-info
	 */
	Zone info(String zone, String primaryNameServer) throws ProviderException {
		try {
			return zc.info(zone, primaryNameServer, customHeaders);
		} catch (DomainrobotApiException e) {
			throw new ProviderException("API exception", e);
		} catch (Exception e) {
			throw new ProviderException("Unknown exception", e);
		}
	}

	/**
	 * Processes the ip settings for the desired zone and subtld,
	 * The corresponding resource record will be updated or removed if null.
	 *
	 * @param zone      the zone
	 * @param sld       the sld
	 * @param ipSetting the ip setting
	 */
	void process(Zone zone, String sld, IpSetting ipSetting) {
		processIPv4(zone, sld, ipSetting.getIpv4());
		processIPv6(zone, sld, ipSetting.getIpv6());
	}

	private void processIPv4(Zone zone, String sld, Inet4Address ip) {
		if(ip != null)
			addOrUpdateIPv4(zone, sld, ip);
		else
			removeIPv4(zone, sld);
	}

	private void processIPv6(Zone zone, String sld, Inet6Address ip) {
		if(ip != null)
			addOrUpdateIPv6(zone, sld, ip);
		else
			removeIPv6(zone, sld);
	}

	private void addOrUpdateIPv4(Zone zone, String sld, Inet4Address ip) {
		addOrUpdateIP(zone, sld, ip, ResouceRecordTypeIP.A);
	}

	private void addOrUpdateIPv6(Zone zone, String sld, Inet6Address ip) {
		addOrUpdateIP(zone, sld, ip, ResouceRecordTypeIP.AAAA);
	}

	private void addOrUpdateIP(Zone zone, String sld, InetAddress ip, ResouceRecordTypeIP type) {
		ResourceRecord rr = searchResourceRecord(zone, sld, type);
		if(rr != null) {
			rr.setValue(ip.getHostAddress());
			rr.setTtl(defaultTtl);
		} else {
			ResourceRecord rrSld = new ResourceRecord();
			rrSld.setName(sld);
			rrSld.setValue(ip.getHostAddress());
			rrSld.setType(type.toString());
			rrSld.setTtl(defaultTtl);
			zone.getResourceRecords().add(rrSld);
		}
	}

	void removeIPv4(Zone zone, String sld) {
		removeIP(zone, sld, ResouceRecordTypeIP.A);
	}

	void removeIPv6(Zone zone, String sld) {
		removeIP(zone, sld, ResouceRecordTypeIP.AAAA);
	}

	void removeIP(Zone zone, String sld, ResouceRecordTypeIP type) {
		ResourceRecord rr = searchResourceRecord(zone, sld, type);
		if(rr != null) {
			zone.getResourceRecords().remove(rr);
			zone.getResourceRecords().remove(rr);
		}
	}
}
