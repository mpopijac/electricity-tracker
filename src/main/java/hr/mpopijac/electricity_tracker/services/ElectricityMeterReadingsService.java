package hr.mpopijac.electricity_tracker.services;

import hr.mpopijac.electricity_tracker.converters.ElectricityMeterReadingModelToDtoConverter;
import hr.mpopijac.electricity_tracker.dto.ElectricityMeterReadingDto;
import hr.mpopijac.electricity_tracker.dto.YearlyEnergyConsumptionDto;
import hr.mpopijac.electricity_tracker.exceptions.ClientNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.ElectricityMeterNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.ElectricityMeterReadingNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.EntityAlreadyExistsException;
import hr.mpopijac.electricity_tracker.models.ClientModel;
import hr.mpopijac.electricity_tracker.models.ElectricityMeterModel;
import hr.mpopijac.electricity_tracker.models.ElectricityMeterReadingModel;
import hr.mpopijac.electricity_tracker.repository.ClientRepository;
import hr.mpopijac.electricity_tracker.repository.ElectricityMeterReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ElectricityMeterReadingsService {

    private final ClientRepository clientRepository;

    private final ElectricityMeterReadingRepository electricityMeterReadingRepository;

    private final ElectricityMeterReadingModelToDtoConverter electricityMeterReadingModelToDtoConverter;

    @Autowired
    public ElectricityMeterReadingsService(ClientRepository clientRepository,
                                           ElectricityMeterReadingRepository electricityMeterReadingRepository,
                                           ElectricityMeterReadingModelToDtoConverter electricityMeterReadingModelToDtoConverter) {
        this.clientRepository = clientRepository;
        this.electricityMeterReadingRepository = electricityMeterReadingRepository;
        this.electricityMeterReadingModelToDtoConverter = electricityMeterReadingModelToDtoConverter;
    }

    /**
     * Method to get aggregate meter reading for selected year
     *
     * @param clientId identifier
     * @param year     selection
     * @return YearlyEnergyConsumptionDto
     * @throws ClientNotFoundException
     */
    public YearlyEnergyConsumptionDto getAggregateMeterReading(final Long clientId, final Integer year) {
        final ElectricityMeterModel electricityMeter = findMeter(clientId);
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = new YearlyEnergyConsumptionDto();
        final Integer consumedEnergy = electricityMeter.getElectricityMeterReadings()
                .stream().filter(emr -> Objects.equals(emr.getYear(), year))
                .mapToInt(ElectricityMeterReadingModel::getConsumedEnergy).sum();
        yearlyEnergyConsumptionDto.setAggregateConsumedEnergy(consumedEnergy);
        yearlyEnergyConsumptionDto.setYear(year);
        return yearlyEnergyConsumptionDto;
    }

    /**
     * Method to get energy consumption per month for selected year
     *
     * @param clientId identifier
     * @param year     selection
     * @param month    (optional), if present then is only returned data for a selected month
     * @return YearlyEnergyConsumptionDto
     * @throws ClientNotFoundException
     */
    public YearlyEnergyConsumptionDto getMeterReadings(final Long clientId, final Integer year, final Optional<Integer> month) {
        final ElectricityMeterModel electricityMeter = findMeter(clientId);
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = new YearlyEnergyConsumptionDto();
        final List<ElectricityMeterReadingDto> electricityMeterReadingDtos = electricityMeter.getElectricityMeterReadings().stream()
                .filter(emr -> Objects.equals(emr.getYear(), year))
                .filter(emr -> !month.isPresent() || emr.getMonth().getValue() == month.get())
                .sorted(Comparator.comparing(ElectricityMeterReadingModel::getMonth))
                .map(emr -> new ElectricityMeterReadingDto(emr.getMonth(), emr.getConsumedEnergy()))
                .collect(Collectors.toList());
        yearlyEnergyConsumptionDto.setConsumptionPerMonth(electricityMeterReadingDtos);
        yearlyEnergyConsumptionDto.setYear(year);
        return yearlyEnergyConsumptionDto;
    }

    /**
     * Method for adding new energy consumption reading
     *
     * @param clientId                   identifier
     * @param electricityMeterReadingDto monthly energy reading data
     * @return newly stored energy consumption reading
     * @throws ClientNotFoundException,
     * @throws ElectricityMeterNotFoundException,
     * @throws EntityAlreadyExistsException
     */
    @Transactional
    public ElectricityMeterReadingDto addMeterReading(final Long clientId, final ElectricityMeterReadingDto electricityMeterReadingDto) {
        final ElectricityMeterModel electricityMeter = findMeter(clientId);
        final ElectricityMeterReadingModel electricityMeterReading = createMeterReading(electricityMeter, electricityMeterReadingDto);
        try {
            final ElectricityMeterReadingModel newElectricityMeterReading = electricityMeterReadingRepository.save(electricityMeterReading);
            return electricityMeterReadingModelToDtoConverter.convert(newElectricityMeterReading);
        } catch (DataIntegrityViolationException e) {
            throw new EntityAlreadyExistsException("ElectricityMeterReading for year:"
                    + electricityMeterReadingDto.getYear() + " and month:" + electricityMeterReadingDto.getMonth()
                    + " already exists.", e);
        }
    }

    /**
     * Update existing electricity meter reading
     *
     * @param clientId                   identifier
     * @param electricityMeterReadingDto update data
     * @throws ClientNotFoundException,
     * @throws ElectricityMeterNotFoundException,
     * @throws ElectricityMeterReadingNotFoundException
     */
    @Transactional
    public void updateMeterReading(final Long clientId, final ElectricityMeterReadingDto electricityMeterReadingDto) {
        final ElectricityMeterModel electricityMeter = findMeter(clientId);
        final ElectricityMeterReadingModel electricityReadingToUpdate = electricityMeter.getElectricityMeterReadings()
                .stream().filter(emr -> emr.getMonth().getValue() == electricityMeterReadingDto.getMonth())
                .filter(emr -> emr.getYear().equals(electricityMeterReadingDto.getYear()))
                .findFirst()
                .orElseThrow(() -> new ElectricityMeterReadingNotFoundException("Electricity meter reading for year:"
                        + electricityMeterReadingDto.getYear() + " and month:"
                        + electricityMeterReadingDto.getMonth() + " is not found."));
        electricityReadingToUpdate.setConsumedEnergy(electricityMeterReadingDto.getConsumedEnergy());
        electricityMeterReadingRepository.save(electricityReadingToUpdate);
    }

    /**
     * Deleting existing electricity meter reading by following parameters:
     *
     * @param clientId identifier
     * @param year     selected
     * @param month    selected
     * @throws ClientNotFoundException,
     * @throws ElectricityMeterNotFoundException,
     * @throws ElectricityMeterReadingNotFoundException
     */
    @Transactional
    public void deleteMeterReading(final Long clientId, final Integer year, final Integer month) {
        final ElectricityMeterModel electricityMeterModel = findMeter(clientId);
        final ElectricityMeterReadingModel electricityMeterReading = findMeterReading(electricityMeterModel, year, month);
        electricityMeterReadingRepository.deleteById(electricityMeterReading.getId());
    }

    private ElectricityMeterModel findMeter(final Long clientId) {
        final ClientModel client = getClient(clientId);
        final Optional<ElectricityMeterModel> electricityMeter = Optional.ofNullable(client.getElectricityMeter());
        return electricityMeter.orElseThrow(() -> new ElectricityMeterNotFoundException("Electricity meter for clientId:"
                + clientId + " is not found."));
    }

    private ElectricityMeterReadingModel findMeterReading(final ElectricityMeterModel electricityMeterModel,
                                                          final Integer year,
                                                          final Integer month) {
        return electricityMeterModel.getElectricityMeterReadings()
                .stream().filter(emr -> emr.getMonth().getValue() == month)
                .filter(emr -> emr.getYear().equals(year))
                .findFirst()
                .orElseThrow(() -> new ElectricityMeterReadingNotFoundException("Electricity meter reading for year:"
                        + year + " and month:" + month + " is not found."));
    }

    private ElectricityMeterReadingModel createMeterReading(final ElectricityMeterModel electricityMeterModel, final ElectricityMeterReadingDto electricityMeterReadingDto) {
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

