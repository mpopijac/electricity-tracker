package hr.mpopijac.electricity_tracker.repository;

import hr.mpopijac.electricity_tracker.models.ClientModel;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends PagingAndSortingRepository<ClientModel, Long> {

}
