package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.HostEnriched;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

/** HostRepo interface is used to perform CRUD operations on the Host entity. */
@Repository
public interface HostRepo extends ListCrudRepository<Host, Integer> {

  /**
   * Finds all hosts with enriched information including zone details.
   *
   * @return a list of all enriched hosts
   */
  @Query(
      "select h.id, h.SLD, h.API_TOKEN, concat(h.SLD, '.', z.NAME) full_host, "
          + "h.ZONE_ID, z.NAME as ZONE, z.NS, h.CHANGED "
          + "from HOST h "
          + "join ZONE z on z.ID = h.ZONE_ID "
          + "order by h.id")
  List<HostEnriched> findAllExtended();

  /**
   * Finds a host by its full host name.
   *
   * @param fullHost the full host name (e.g., subdomain.example.com)
   * @return an optional enriched host
   */
  @Query(
      "select h.id, h.SLD, concat(h.SLD, '.', z.NAME) full_host, h.API_TOKEN, h.ZONE_ID, "
          + " z.NAME as ZONE, z.NS, h.CHANGED from HOST h "
          + "join PUBLIC.ZONE z on z.ID = h.ZONE_ID "
          + "where concat(h.SLD, '.', z.NAME) = :fullHost "
          + "order by h.id")
  Optional<HostEnriched> findByFullHost(String fullHost);

  /**
   * Finds all hosts by zone ID.
   *
   * @param zoneId the zone ID to filter by
   * @return a list of enriched hosts for the specified zone
   */
  @Query(
      "select h.id, h.SLD, concat(h.SLD, '.', z.NAME) full_host, h.API_TOKEN, h.ZONE_ID, "
          + " z.NAME as ZONE, z.NS, h.CHANGED from HOST h "
          + "join PUBLIC.ZONE z on z.ID = h.ZONE_ID "
          + "where z.ID = :zoneId "
          + "order by h.id")
  List<HostEnriched> findByZoneId(Integer zoneId);
}
