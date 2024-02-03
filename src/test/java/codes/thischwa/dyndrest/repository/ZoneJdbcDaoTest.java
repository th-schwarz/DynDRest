package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.Zone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

class ZoneJdbcDaoTest extends AbstractIntegrationTest {

  @Autowired private ZoneJdbcDao dao;

  @Test
  void testGetByName() {
    Zone zone = dao.getByName("dynhost1.info");
    assertEquals(2, zone.getId());
    assertEquals("ns1.domain.info", zone.getNs());
    assertEquals("2024-01-28T12:00:37.013707", zone.getChanged().toString());

    assertThrows(EmptyResultDataAccessException.class, () -> dao.getByName("unknown"));
  }

}
