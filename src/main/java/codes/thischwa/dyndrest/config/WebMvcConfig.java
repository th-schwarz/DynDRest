package codes.thischwa.dyndrest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		// can't be implemented in a generic way, otherwise application context won't be initialized!
		registry.addConverter(new Inet4AdrConverter());
		registry.addConverter(new Inet6AdrConverter());
	}

	private static class Inet4AdrConverter implements Converter<String, Inet4Address> {

		@Override
		public Inet4Address convert(String source) {
			if(ObjectUtils.isEmpty(source))
				return null;
			try {
				return (Inet4Address) InetAddress.getByName(source);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	private static class Inet6AdrConverter implements Converter<String, Inet6Address> {

		@Override
		public Inet6Address convert(String source) {
			if(ObjectUtils.isEmpty(source))
				return null;
			try {
				return (Inet6Address) InetAddress.getByName(source);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
}
