package hr.mpopijac.electricity_tracker.repository;

import hr.mpopijac.electricity_tracker.models.ElectricityMeterReadingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricityMeterReadingRepository extends JpaRepository<ElectricityMeterReadingModel, Long> {

}
