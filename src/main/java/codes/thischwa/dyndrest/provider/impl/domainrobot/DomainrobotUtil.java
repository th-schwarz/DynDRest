package codes.thischwa.dyndrest.provider.impl.domainrobot;

import org.domainrobot.sdk.models.generated.ResourceRecord;
import org.domainrobot.sdk.models.generated.Zone;

/**
 * A static utility class mainly for the Zone object of the domain-robot sdk.
 */
interface DomainrobotUtil {

	long DEFAULT_TLD = 60;

	static void addOrUpdateIPv4(Zone zone, String sld, String ip) {
		addOrUpdateIP(zone, sld, ip, ResouceRecordTypeIP.A);
	}

	static void addOrUpdateIPv6(Zone zone, String sld, String ip) {
		addOrUpdateIP(zone, sld, ip, ResouceRecordTypeIP.AAAA);
	}

	static void removeIPv4(Zone zone, String sld) {
		removeIP(zone, sld, ResouceRecordTypeIP.A);
	}

	static void removeIPv6(Zone zone, String sld) {
		removeIP(zone, sld, ResouceRecordTypeIP.AAAA);
	}

	private static void removeIP(Zone zone, String sld, ResouceRecordTypeIP type) {
		ResourceRecord rr = searchResourceRecord(zone, sld, type);
		if(rr != null) {
			zone.getResourceRecords().remove(rr);
		}
	}

	private static void addOrUpdateIP(Zone zone, String sld, String ip, ResouceRecordTypeIP type) {
		ResourceRecord rr = searchResourceRecord(zone, sld, type);
		if(rr != null) {
			rr.setValue(ip);
			rr.setTtl(DEFAULT_TLD);
		} else {
			ResourceRecord rrSld = new ResourceRecord();
			rrSld.setName(sld);
			rrSld.setValue(ip);
			rrSld.setType(type.toString());
			rrSld.setTtl(DEFAULT_TLD);
			zone.getResourceRecords().add(rrSld);
		}
	}

	static ResourceRecord searchResourceRecord(Zone zone, String name, ResouceRecordTypeIP type) {
		return zone.getResourceRecords().stream().filter(rr -> rr.getType().equals(type.toString()) && rr.getName().equals(name))
				.findFirst().orElse(null);
	}

	static String deriveZone(String host) {
		long cnt = host.chars().filter(ch -> ch == '.').count();
		if(cnt < 2)
			throw new IllegalArgumentException("'host' must be a sub domain.");
		return host.substring(host.indexOf(".") + 1);
	}

	static boolean hasIPsChanged(Zone zone, String sld, String ipv4, String ipv6) {
		ResourceRecord rrv4 = searchResourceRecord(zone, sld, ResouceRecordTypeIP.A);
		ResourceRecord rrv6 = searchResourceRecord(zone, sld, ResouceRecordTypeIP.AAAA);
		boolean ipv4Changed = !hasIPChanged(rrv4, ipv4);
		boolean ipv6Changed = !hasIPChanged(rrv6, ipv6);
		return ipv4Changed || ipv6Changed;
	}

	private static boolean hasIPChanged(ResourceRecord rr, String ip) {
		return rr != null && ip != null && rr.getValue().equals(ip);
	}

	enum ResouceRecordTypeIP {
		A, AAAA
	}
}
