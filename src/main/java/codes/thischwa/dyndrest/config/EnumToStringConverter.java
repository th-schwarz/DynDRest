package codes.thischwa.dyndrest.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

// Convert from Enum to string
@WritingConverter
public class EnumToStringConverter<E extends Enum> implements Converter<E, String> {

  @Override
  public String convert(E source) {
    return source.toString();
  }
}
