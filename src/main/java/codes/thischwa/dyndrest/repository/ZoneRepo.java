package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.Zone;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

/**
 * ZoneRepo is a repository interface that performs CRUD operations for the Zone class. It extends
 * the ListCrudRepository interface. It provides methods to query and retrieve Zone objects from the
 * underlying data source.
 */
@Repository
public interface ZoneRepo extends ListCrudRepository<Zone, Integer> {

  /**
   * Finds a zone by its name.
   *
   * @param name the zone name to search for
   * @return the zone with the specified name, or null if not found
   */
  @Query("select z.ID, NAME, NS, CHANGED from ZONE as z where NAME=:name")
  @Nullable
  Zone findByName(String name);
}
