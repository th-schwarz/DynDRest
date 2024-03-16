package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class UpdateLogRepoTest extends AbstractIntegrationTest {

  @Autowired private UpdateLogRepo repo;

  @Test
  void testFindAllByStatus() {
    assertEquals(43, repo.findAll().size());
    List<FullUpdateLog> logs = repo.findAllByStatus(UpdateLog.Status.virgin);
    assertEquals(42, logs.size());
  }

  @Test
  void testFindById() {
    List<FullUpdateLog> hosts = repo.findAllFullUpdateLogsByIds(List.of(43, 42));
    assertEquals(2, hosts.size());
  }

  @Test
  void testPagination() {
    Page<UpdateLog> page =
        repo.findAll(PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "changed")));
    assertEquals(43, page.getTotalElements());
    assertEquals(4, page.getSize());
    assertEquals(11, page.getTotalPages());
    assertEquals(0, page.getNumber());
    assertEquals(4, page.getNumberOfElements());
    assertTrue(page.isFirst());
    assertFalse(page.isLast());

    assertEquals(43, page.getContent().get(0).getId());
    assertEquals(42, page.getContent().get(1).getId());
    assertEquals(41, page.getContent().get(2).getId());
    assertEquals(40, page.getContent().get(3).getId());

    page = repo.findAll(PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "changed")));
    assertEquals(39, page.getContent().get(0).getId());
  }
}
