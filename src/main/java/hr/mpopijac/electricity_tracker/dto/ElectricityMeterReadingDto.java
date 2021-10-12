package hr.mpopijac.electricity_tracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;


@Data
@NoArgsConstructor
public class ElectricityMeterReadingDto {

    private Long id;

    private Integer month;

    private Integer consumedEnergy;

    private Integer year;

    public ElectricityMeterReadingDto(Month month, Integer consumedEnergy) {
        this.month = month.getValue();
        this.consumedEnergy = consumedEnergy;
    }
}
