package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UpdateLogRepoTest extends AbstractIntegrationTest {

  @Autowired private UpdateLogRepo repo;

  @BeforeAll
  void init() throws UnknownHostException {
    repo.save(UpdateLog.getInstance(1, new IpSetting("198.0.1.0", "2a03:4000:41:32:0:0:1:0")));
    repo.save(UpdateLog.getInstance(2, new IpSetting("198.0.2.0", "2a03:4000:41:32:0:0:2:0")));
    for (int i = 1; i <= 20; i++) {
      repo.save(UpdateLog.getInstance(1, new IpSetting("198.0.1." + i)));
      repo.save(UpdateLog.getInstance(2, new IpSetting("198.0.2." + i)));
    }
  }

  @Test
  void testFindByStatus() {
    List<FullUpdateLog> logs = repo.findByStatus(UpdateLog.Status.NEW);
    assertEquals(42, logs.size());
  }
}
