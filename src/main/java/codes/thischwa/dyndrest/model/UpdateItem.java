package codes.thischwa.dyndrest.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class UpdateItem implements Comparable<UpdateItem> {

	private final String dateTime;

	private final String host;

	private final String ipv4;

	private final String ipv6;

	public UpdateItem(String dateTime, String host, String ipv4, String ipv6) {
		this.dateTime = dateTime;
		this.host = host;
		this.ipv4 = ipv4 == null ? "n/a" : ipv4;
		this.ipv6 = ipv6 == null ? "n/a" : ipv6;
	}

	@Override
	public String toString() {
		return "UpdateItem [dateTime=" + dateTime + ", host=" + host + ", ipv4=" + ipv4 + ", ipv6=" + ipv6 + "]";
	}

	@Override
	public int compareTo(UpdateItem o2) {
		return dateTime.compareTo(o2.getDateTime());
	}

	@Override
	public int hashCode() {
		return Objects.hash(dateTime, host, ipv4, ipv6);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof UpdateItem))
			return false;
		UpdateItem other = (UpdateItem) obj;
		return Objects.equals(dateTime, other.dateTime) && Objects.equals(host, other.host) && Objects.equals(ipv4, other.ipv4)
				&& Objects.equals(ipv6, other.ipv6);
	}

}