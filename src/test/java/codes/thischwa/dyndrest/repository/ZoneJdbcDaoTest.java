package codes.thischwa.dyndrest.repository;

import static org.junit.jupiter.api.Assertions.*;

import codes.thischwa.dyndrest.GenericIntegrationTest;
import codes.thischwa.dyndrest.model.Zone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

class ZoneJdbcDaoTest extends GenericIntegrationTest {

  @Autowired private ZoneJdbcDao dao;

  @Test
  void testGetByName() {
    Zone zone = dao.getByName("dynhost1.info");
    assertEquals("ns1.domain.info", zone.getNs());

    assertThrows(EmptyResultDataAccessException.class, () -> dao.getByName("unknown"));
  }

}
