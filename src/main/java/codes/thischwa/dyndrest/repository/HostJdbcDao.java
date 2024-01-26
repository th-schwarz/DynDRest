package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.Host;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * The HostJdbcDao class is a repository class that performs database operations related to the Host
 * entity.
 */
@Repository
public class HostJdbcDao {

  private static final String sql_all = "SELECT * FROM host order by id";

  private static final String sql_all_extended =
      "select h.id, h.NAME, h.API_TOKEN, concat(h.NAME, '.', z.NAME) full_host, "
          + "h.ZONE_ID, z.NAME as ZONE, z.NS, h.CHANGED "
          + "from HOST h "
          + "join ZONE z on z.ID = h.ZONE_ID "
          + "order by h.id";

  private static final String sql_full_host =
      "select h.id, h.NAME, concat(h.NAME, '.', z.NAME) full_host, h.API_TOKEN, h.ZONE_ID, "
          + " z.NAME as ZONE, z.NS, h.CHANGED from HOST h "
          + "join PUBLIC.ZONE z on z.ID = h.ZONE_ID "
          + "where concat(h.NAME, '.', z.NAME) = ?";

  private final JdbcTemplate jdbcTemplate;

  public HostJdbcDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Host> getAll() {
    return jdbcTemplate.query(sql_all, new BeanPropertyRowMapper<>(Host.class));
  }

  public List<Host> getAllExtended() {
    return jdbcTemplate.query(sql_all_extended, new BeanPropertyRowMapper<>(Host.class));
  }

  public Host getByFullHost(String fullHost) throws EmptyResultDataAccessException {
    return (Host)
        jdbcTemplate.queryForObject(sql_full_host, new BeanPropertyRowMapper(Host.class), fullHost);
  }
}
