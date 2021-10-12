package hr.mpopijac.electricity_tracker.converters;

import hr.mpopijac.electricity_tracker.dto.ElectricityMeterReadingDto;
import hr.mpopijac.electricity_tracker.models.ElectricityMeterReadingModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ElectricityMeterReadingModelToDtoConverter implements Converter<ElectricityMeterReadingModel, ElectricityMeterReadingDto> {

    @Override
    public ElectricityMeterReadingDto convert(ElectricityMeterReadingModel meterReading) {
        ElectricityMeterReadingDto electricityMeterReadingDto = new ElectricityMeterReadingDto();
        electricityMeterReadingDto.setId(meterReading.getId());
        electricityMeterReadingDto.setMonth(meterReading.getMonth().getValue());
        electricityMeterReadingDto.setConsumedEnergy(meterReading.getConsumedEnergy());
        electricityMeterReadingDto.setYear(meterReading.getYear());
        return electricityMeterReadingDto;
    }
}
