package codes.thischwa.dyndrest.server.config;

import codes.thischwa.dyndrest.util.NetUtil;
import jakarta.annotation.Nullable;
import java.net.InetAddress;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** The mvc config. */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new InetAdrConverter());
    // enum conversion
    ApplicationConversionService.configure(registry);
  }

  private static class InetAdrConverter implements Converter<String, InetAddress> {

    @Override
    public @Nullable InetAddress convert(String source) {
      if (ObjectUtils.isEmpty(source)) {
        return null;
      }
      return NetUtil.convert(source);
    }
  }
}
