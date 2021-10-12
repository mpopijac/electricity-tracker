package hr.mpopijac.electricity_tracker.converters;


import hr.mpopijac.electricity_tracker.dto.ElectricityMeterReadingDto;
import hr.mpopijac.electricity_tracker.models.ElectricityMeterReadingModel;
import org.junit.Before;
import org.junit.Test;

import java.time.Month;

import static org.junit.Assert.assertEquals;

public class ElectricityMeterReadingModelToDtoConverterTest {

    private ElectricityMeterReadingModelToDtoConverter electricityMeterReadingModelToDtoConverter;

    @Before
    public void setUp() {
        electricityMeterReadingModelToDtoConverter = new ElectricityMeterReadingModelToDtoConverter();
    }

    @Test
    public void convert() {
        final ElectricityMeterReadingModel electricityMeterReading = new ElectricityMeterReadingModel();
        electricityMeterReading.setId(100L);
        electricityMeterReading.setYear(2020);
        electricityMeterReading.setMonth(Month.OCTOBER);
        electricityMeterReading.setConsumedEnergy(213);

        final ElectricityMeterReadingDto meterReadingDto = electricityMeterReadingModelToDtoConverter.convert(electricityMeterReading);

        assertEquals(100, meterReadingDto.getId().intValue());
        assertEquals(2020, meterReadingDto.getYear().intValue());
        assertEquals(10, meterReadingDto.getMonth().intValue());
        assertEquals(213L, meterReadingDto.getConsumedEnergy().intValue());
    }
}
