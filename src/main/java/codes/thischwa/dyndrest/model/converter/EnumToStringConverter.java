package codes.thischwa.dyndrest.model.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

/**
 * The EnumToStringConverter class is a converter that converts an enum value to its string
 * representation.
 *
 * @param <E> the enum type
 */
@WritingConverter
public class EnumToStringConverter<E extends Enum<?>> implements Converter<E, String> {

  @Override
  public String convert(E source) {
    return source.toString();
  }
}
