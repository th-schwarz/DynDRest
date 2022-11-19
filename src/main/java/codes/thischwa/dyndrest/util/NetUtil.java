package codes.thischwa.dyndrest.util;

import codes.thischwa.dyndrest.model.IpSetting;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Some network relevant utils.
 */
public interface NetUtil {

	static boolean isIP(String ipStr) {
		return NetUtil.isIPv4(ipStr) || NetUtil.isIPv6(ipStr);
	}

	static boolean isIPv4(String ipStr) {
		try {
			return (InetAddress.getByName(ipStr) instanceof Inet4Address);
		} catch (UnknownHostException e) {
			return false;
		}
	}

	static boolean isIPv6(String ipStr) {
		try {
			return (InetAddress.getByName(ipStr) instanceof Inet6Address);
		} catch (UnknownHostException e) {
			return false;
		}
	}

	static String buildBasicAuth(String user, String pwd) {
		String authStr = String.format("%s:%s", user, pwd);
		String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));
		return "Basic " + base64Creds;
	}

	static String getBaseUrl(boolean forceHttps) {
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		if(forceHttps)
			builder.scheme("https");
		return builder.replacePath(null).build().toUriString();
	}

	/**
	 * Resolves the ip settings of the desired 'hostName'.
	 *
	 * @param hostName the host name
	 * @return the ip setting
	 * @throws IOException if the resolving fails
	 */
	static IpSetting resolve(String hostName) throws IOException {
		IpSetting ipSetting = new IpSetting();
		Record rec = lookup(hostName, Type.A);
		if(rec != null) {
			ARecord aRec = (ARecord) rec;
			ipSetting.setIpv4((Inet4Address) aRec.getAddress());
		}

		rec = lookup(hostName, Type.AAAA);
		if(rec != null)
			ipSetting.setIpv6((Inet6Address) ((AAAARecord) rec).getAddress());
		return ipSetting;
	}

	private static Record lookup(String hostName, int type) throws IOException {
		try {
			Record[] records = new Lookup(hostName, type).run();
			return (records == null || records.length == 0) ? null : records[0];
		} catch (TextParseException e) {
			throw new IOException(String.format("Couldn't lookup %s", hostName), e);
		}
	}

}
