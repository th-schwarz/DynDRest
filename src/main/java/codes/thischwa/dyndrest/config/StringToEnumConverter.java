package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.model.UpdateLog;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

// Convert from string to Enum
@ReadingConverter
public class StringToEnumConverter implements Converter<String, UpdateLog.Status> {

  @Override
  public UpdateLog.Status convert(String source) {
    return Enum.valueOf(UpdateLog.Status.class, source.trim().toUpperCase());
  }
}
