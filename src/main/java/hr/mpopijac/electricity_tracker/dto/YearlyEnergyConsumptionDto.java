package hr.mpopijac.electricity_tracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class YearlyEnergyConsumptionDto {

    private Integer year;

    private Integer aggregateConsumedEnergy;

    private List<ElectricityMeterReadingDto> consumptionPerMonth;
}
