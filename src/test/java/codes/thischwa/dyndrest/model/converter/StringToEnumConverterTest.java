package codes.thischwa.dyndrest.model.converter;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.model.UpdateLog;
import org.junit.jupiter.api.Test;

class StringToEnumConverterTest {

  @Test
  void testConvert() {
    StringToEnumConverter<UpdateLog.Status> converter =
        new StringToEnumConverter<>(UpdateLog.Status.class);
    UpdateLog.Status result = converter.convert(" failed");
    assertEquals(UpdateLog.Status.failed, result);
  }
}
