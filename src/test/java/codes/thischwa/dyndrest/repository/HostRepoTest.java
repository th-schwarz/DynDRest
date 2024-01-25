package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.Host;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class HostRepoTest extends GenericIntegrationTest {

  @Autowired private HostRepo hostFullRepo;

  @Test
  void testFindAll() {
    List<Host> hosts = hostFullRepo.findAll();
    assertNotNull(hosts);
    assertEquals(4, hosts.size());
  }

  //  @Test
  //  void testFindAllFlat() {
  //    List<FlatHost> hosts = hostFullRepo.findAllFlat();
  //    assertNotNull(hosts);
  //    assertEquals(4, hosts.size());
  //  }
}
