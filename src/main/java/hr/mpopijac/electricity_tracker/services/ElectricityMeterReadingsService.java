package hr.mpopijac.electricity_tracker.services;

import hr.mpopijac.electricity_tracker.converters.ElectricityMeterReadingModelToDtoConverter;
import hr.mpopijac.electricity_tracker.dto.ElectricityMeterReadingDto;
import hr.mpopijac.electricity_tracker.dto.YearlyEnergyConsumptionDto;
import hr.mpopijac.electricity_tracker.exceptions.ClientNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.ElectricityMeterNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.EntityAlreadyExistsException;
import hr.mpopijac.electricity_tracker.models.ClientModel;
import hr.mpopijac.electricity_tracker.models.ElectricityMeterModel;
import hr.mpopijac.electricity_tracker.models.ElectricityMeterReadingModel;
import hr.mpopijac.electricity_tracker.repository.ClientRepository;
import hr.mpopijac.electricity_tracker.repository.ElectricityMeterReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ElectricityMeterReadingsService {

    private ClientRepository clientRepository;

    private ElectricityMeterReadingRepository electricityMeterReadingRepository;

    private ElectricityMeterReadingModelToDtoConverter electricityMeterReadingModelToDtoConverter;

    @Autowired
    public ElectricityMeterReadingsService(ClientRepository clientRepository,
                                           ElectricityMeterReadingRepository electricityMeterReadingRepository,
                                           ElectricityMeterReadingModelToDtoConverter electricityMeterReadingModelToDtoConverter) {
        this.clientRepository = clientRepository;
        this.electricityMeterReadingRepository = electricityMeterReadingRepository;
        this.electricityMeterReadingModelToDtoConverter = electricityMeterReadingModelToDtoConverter;
    }

    public YearlyEnergyConsumptionDto getAggregateMeterReading(final Long clientId, final Integer year) {

        final ClientModel client = getClient(clientId);
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = new YearlyEnergyConsumptionDto();
        yearlyEnergyConsumptionDto.setYear(year);

        if (client.getElectricityMeter() != null) {
            final Integer consumedEnergy = client.getElectricityMeter().getElectricityMeterReadings()
                    .stream().filter(emr -> Objects.equals(emr.getYear(), year))
                    .mapToInt(ElectricityMeterReadingModel::getConsumedEnergy).sum();
            yearlyEnergyConsumptionDto.setAggregateConsumedEnergy(consumedEnergy);
        } else {
            yearlyEnergyConsumptionDto.setAggregateConsumedEnergy(0);
        }

        return yearlyEnergyConsumptionDto;
    }

    public YearlyEnergyConsumptionDto getMeterReadings(final Long clientId, final Integer year, final Integer month) {
        final ClientModel client = getClient(clientId);

        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = new YearlyEnergyConsumptionDto();
        yearlyEnergyConsumptionDto.setYear(year);

        if (client.getElectricityMeter() != null) {
            List<ElectricityMeterReadingDto> electricityMeterReadingDtos = client.getElectricityMeter().getElectricityMeterReadings().stream()
                    .filter(emr -> Objects.equals(emr.getYear(), year))
                    .filter(emr -> month == null || emr.getMonth().getValue() == month)
                    .map(emr -> new ElectricityMeterReadingDto(emr.getMonth(), emr.getConsumedEnergy()))
                    .collect(Collectors.toList());
            yearlyEnergyConsumptionDto.setConsumptionPerMonth(electricityMeterReadingDtos);
        }

        return yearlyEnergyConsumptionDto;
    }

    public ElectricityMeterReadingDto addMeterReading(Long clientId, ElectricityMeterReadingDto electricityMeterReadingDto) {
        final ClientModel client = getClient(clientId);
        final ElectricityMeterModel electricityMeterModel = client.getElectricityMeter();
        if (electricityMeterModel != null) {
            final ElectricityMeterReadingModel electricityMeterReading = createMeterReading(electricityMeterModel, electricityMeterReadingDto);
            try {
                final ElectricityMeterReadingModel newElectricityMeterReading = electricityMeterReadingRepository.save(electricityMeterReading);
                return electricityMeterReadingModelToDtoConverter.convert(newElectricityMeterReading);
            } catch (DataIntegrityViolationException e) {
                throw new EntityAlreadyExistsException("ElectricityMeterReading for year:"
                        + electricityMeterReadingDto.getYear() + " and month:" + electricityMeterReadingDto.getMonth()
                        + " already exists.", e);
            }
        } else {
            throw new ElectricityMeterNotFoundException("Electricity meter for clientId:" + clientId + " is not found.");
        }
    }

    private ElectricityMeterReadingModel createMeterReading(ElectricityMeterModel electricityMeterModel, ElectricityMeterReadingDto electricityMeterReadingDto) {
        final ElectricityMeterReadingModel electricityMeterReading = new ElectricityMeterReadingModel();
        electricityMeterReading.setElectricityMeter(electricityMeterModel);
        electricityMeterReading.setYear(electricityMeterReadingDto.getYear());
        electricityMeterReading.setConsumedEnergy(electricityMeterReadingDto.getConsumedEnergy());
        electricityMeterReading.setMonth(Month.of(electricityMeterReadingDto.getMonth()));
        return electricityMeterReading;
    }

    private ClientModel getClient(final Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client with id:" + clientId + " is not found."));
    }


}

