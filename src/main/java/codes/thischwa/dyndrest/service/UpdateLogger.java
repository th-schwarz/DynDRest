package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.model.IpSetting;

/** Interface for logging zone updates. */
public interface UpdateLogger {

  void log(String host, IpSetting ipSetting);
}
