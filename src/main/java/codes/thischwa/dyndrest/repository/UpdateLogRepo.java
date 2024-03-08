package codes.thischwa.dyndrest.repository;

import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.model.UpdateLog;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdateLogRepo
    extends ListPagingAndSortingRepository<UpdateLog, Integer>,
        ListCrudRepository<UpdateLog, Integer> {

  @Query(
      "select u.ID, u.HOST_ID, u.IPV4, u.IPV6, u.CHANGED, u.CHANGED_UPDATE, u.STATUS, concat(h.NAME, '.', z.NAME) host "
              + "from UPDATE_LOG u "
              + " join PUBLIC.HOST h on h.ID = u.HOST_ID "
              + "join PUBLIC.ZONE z on z.ID = h.ZONE_ID "
              + "where u.STATUS = :status "
              + "order by u.id")
  List<FullUpdateLog> findByStatus(UpdateLog.Status status);
}
