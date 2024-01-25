package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.Host;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

/** The interface Host full repo. */
@Repository
public interface HostRepo extends ListCrudRepository<Host, Integer> {

  /*@Query(
      "select h.id, concat(h.NAME, '.', z.NAME) as host, z.NS, h.CHANGED from HOST as h "
          + "join ZONE z on z.ID = h.ZONE_ID")
  List<FlatHost> findAllFlat();*/
}
