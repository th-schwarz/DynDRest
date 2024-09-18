package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.Host;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

/** HostRepo interface is used to perform CRUD operations on the Host entity. */
@Repository
public interface HostRepo extends ListCrudRepository<Host, Integer> {

  @Query(
      "select h.id, h.NAME, h.API_TOKEN, concat(h.NAME, '.', z.NAME) full_host, "
          + "h.ZONE_ID, z.NAME as ZONE, z.NS, h.CHANGED "
          + "from HOST h "
          + "join ZONE z on z.ID = h.ZONE_ID "
          + "order by h.id")
  List<HostEnriched> findAllExtended();

  @Query(
      "select h.id, h.NAME, concat(h.NAME, '.', z.NAME) full_host, h.API_TOKEN, h.ZONE_ID, "
          + " z.NAME as ZONE, z.NS, h.CHANGED from HOST h "
          + "join PUBLIC.ZONE z on z.ID = h.ZONE_ID "
          + "where concat(h.NAME, '.', z.NAME) = :fullHost "
          + "order by h.id")
  Optional<HostEnriched> findByFullHost(String fullHost);

  @Query(
      "select h.id, h.NAME, concat(h.NAME, '.', z.NAME) full_host, h.API_TOKEN, h.ZONE_ID, "
          + " z.NAME as ZONE, z.NS, h.CHANGED from HOST h "
          + "join PUBLIC.ZONE z on z.ID = h.ZONE_ID "
          + "where z.ID = :zoneId "
          + "order by h.id")
  List<HostEnriched> findByZoneId(Integer zoneId);
}
