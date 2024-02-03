package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.Zone;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * The ZoneJdbcDao class is responsible for accessing and manipulating Zone objects in the database.
 */
@Repository
public class ZoneJdbcDao {

  private static final String sql_byname =
      "select z.ID, NAME, NS, CHANGED from ZONE as z where NAME=?";

  private final JdbcTemplate jdbcTemplate;

  public ZoneJdbcDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Zone getByName(String name) throws EmptyResultDataAccessException {
    return jdbcTemplate.queryForObject(
        sql_byname, BeanPropertyRowMapper.newInstance(Zone.class), name);
  }
}
