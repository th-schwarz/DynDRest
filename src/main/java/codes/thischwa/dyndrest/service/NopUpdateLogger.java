package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.IpSetting;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * NOP instance for the {@link UpdateLogger}.
 */
@Service
@ConditionalOnProperty(name = "dyndrest.update-log-page-enabled", havingValue = "false")
public class NopUpdateLogger implements UpdateLogger {

  @Override
  public void log(String host, IpSetting ipSetting) {
  }
}
