package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.UpdateLogEnriched;
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
    assertEquals(42, repo.findAll().size());
    List<UpdateLogEnriched> logs = repo.findAllByStatus(UpdateLog.Status.failed);
    assertEquals(2, logs.size());
  }

  @Test
  void testFindById() {
    List<UpdateLogEnriched> logs = repo.findAllFullUpdateLogsByIds(List.of(41, 42));
    assertEquals(2, logs.size());
  }

  @Test
  void testFindByHostId() {
    List<UpdateLogEnriched> logs = repo.findByHostId(1);
    assertFalse(logs.isEmpty());
  }

  @Test
  void testPagination() {
    Page<UpdateLog> page =
        repo.findAll(PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "changed")));
    assertEquals(42, page.getTotalElements());
    assertEquals(4, page.getSize());
    assertEquals(11, page.getTotalPages());
    assertEquals(0, page.getNumber());
    assertEquals(4, page.getNumberOfElements());
    assertTrue(page.isFirst());
    assertFalse(page.isLast());

    assertEquals(42, page.getContent().get(0).getId());
    assertEquals(41, page.getContent().get(1).getId());
    assertEquals(40, page.getContent().get(2).getId());
    assertEquals(39, page.getContent().get(3).getId());

    page = repo.findAll(PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "changed")));
    assertEquals(38, page.getContent().get(0).getId());
  }
}
