package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.model.UpdateLog;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * The UpdateLogRepo interface is a repository interface that provides methods for accessing and
 * manipulating UpdateLog entities. It extends ListPagingAndSortingRepository and ListCrudRepository
 * interfaces.
 */
@Repository
public interface UpdateLogRepo
    extends ListPagingAndSortingRepository<UpdateLog, Integer>,
        ListCrudRepository<UpdateLog, Integer> {

  @Query(
      "select u.ID, u.HOST_ID, u.IPV4, u.IPV6, u.CHANGED, u.CHANGED_UPDATE, u.STATUS, "
          + "concat(h.NAME, '.', z.NAME) host "
          + "from UPDATE_LOG u "
          + "join HOST h on h.ID = u.HOST_ID "
          + "join ZONE z on z.ID = h.ZONE_ID "
          + "where u.STATUS in (:status) "
          + "order by u.CHANGED DESC")
  List<FullUpdateLog> findAllByStatus(UpdateLog.Status... status);

  @Query(
      "select u.ID, u.HOST_ID, u.IPV4, u.IPV6, u.CHANGED, u.CHANGED_UPDATE, u.STATUS, "
          + "concat(h.NAME, '.', z.NAME) host "
          + "from UPDATE_LOG u "
          + "join HOST h on h.ID = u.HOST_ID "
          + "join ZONE z on z.ID = h.ZONE_ID "
          + "where u.ID in (:ids) "
          + "order by u.CHANGED DESC")
  List<FullUpdateLog> findAllFullUpdateLogsByIds(List<Integer> ids);

  @Query(
      "select u.ID, u.HOST_ID, u.IPV4, u.IPV6, u.CHANGED, u.CHANGED_UPDATE, u.STATUS, "
          + "concat(h.NAME, '.', z.NAME) host "
          + "from UPDATE_LOG u "
          + "join HOST h on h.ID = u.HOST_ID "
          + "join ZONE z on z.ID = h.ZONE_ID "
          + "order by u.CHANGED DESC")
  List<FullUpdateLog> findAllFullUpdateLogs();
}
