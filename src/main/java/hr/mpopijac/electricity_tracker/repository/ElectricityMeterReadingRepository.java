package hr.mpopijac.electricity_tracker.repository;

import hr.mpopijac.electricity_tracker.models.ElectricityMeterReadingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricityMeterReadingRepository extends JpaRepository<ElectricityMeterReadingModel, Long> {

    @Modifying
    @Query("delete from ElectricityMeterReadingModel where id = :id")
    void deleteById(@Param("id") Long id);
}
