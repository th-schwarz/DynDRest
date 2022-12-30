package codes.thischwa.dyndrest.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class IpSetting {

	private Inet4Address ipv4;

	private Inet6Address ipv6;

	public IpSetting() {
	}

	public IpSetting(String ipv4Str, String ipv6Str) throws UnknownHostException {
		if(ipv4Str != null)
			ipv4 = (Inet4Address) InetAddress.getByName(ipv4Str);
		if(ipv6Str != null)
			ipv6 = (Inet6Address) InetAddress.getByName(ipv6Str);
	}

	public IpSetting(Inet4Address ipv4, Inet6Address ipv6) {
		this.ipv4 = ipv4;
		this.ipv6 = ipv6;
	}

	public IpSetting(String ipStr) throws UnknownHostException {
		InetAddress ip = InetAddress.getByName(ipStr);
		if(ip instanceof Inet4Address)
			this.ipv4 = (Inet4Address) ip;
		else
			this.ipv6 = (Inet6Address) ip;
	}

	public boolean isNotSet() {
		return ipv4 == null && ipv6 == null;
	}

	@JsonGetter("ipv4")
	public String ipv4ToString() {
		return ipv4 == null ? null : ipv4.getHostAddress();
	}

	@JsonGetter("ipv6")
	public String ipv6ToString() {
		return ipv6 == null ? null : ipv6.getHostAddress();
	}

}