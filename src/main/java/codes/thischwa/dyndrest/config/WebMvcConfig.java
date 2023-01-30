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
		registry.addConverter((Converter<String, Inet4Address>) from -> {
			if(ObjectUtils.isEmpty(from))
				return null;
			try {
				return (Inet4Address) InetAddress.getByName(from);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException(e);
			}
		});

		registry.addConverter((Converter<String, Inet6Address>) from -> {
			if(ObjectUtils.isEmpty(from))
				return null;
			try {
				return (Inet6Address) InetAddress.getByName(from);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException(e);
			}
		});
	}
}
