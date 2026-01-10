package codes.thischwa.dyndrest.model.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * The StringToEnumConverter class is a converter class that converts a string representation to an
 * enum value.
 *
 * @param <T> the enum type
 */
@ReadingConverter
public class StringToEnumConverter<T extends Enum<T>> implements Converter<String, T> {

  private final Class<T> cls;

  public StringToEnumConverter(Class<T> cls) {
    this.cls = cls;
  }

  @Override
  public T convert(String source) {
    String sanitizedSource = source.trim();
    return Enum.valueOf(cls, sanitizedSource);
  }
}
