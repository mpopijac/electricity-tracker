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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ElectricityMeterReadingsServiceTest {

    private static final Long CLIENT_ID = 999L;

    private static final Integer YEAR = 2020;

    @InjectMocks
    private ElectricityMeterReadingsService electricityMeterReadingsService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ElectricityMeterReadingRepository electricityMeterReadingRepository;

    @Mock
    private ElectricityMeterReadingModelToDtoConverter electricityMeterReadingModelToDtoConverter;

    private ClientModel client;

    @Before
    public void setUp() {
        client = new ClientModel();
        client.setId(CLIENT_ID);
    }

    @Test(expected = ElectricityMeterNotFoundException.class)
    public void getAggregateMeterReading_clientWithoutElectricityMeter() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        electricityMeterReadingsService.getAggregateMeterReading(CLIENT_ID, YEAR);
    }

    @Test
    public void getAggregateMeterReading_clientWithoutElectricityMeterReadings() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getAggregateMeterReading(CLIENT_ID, YEAR);

        assertEquals(YEAR, yearlyEnergyConsumptionDto.getYear());
        assertEquals(0, yearlyEnergyConsumptionDto.getAggregateConsumedEnergy().intValue());
    }

    @Test
    public void getAggregateMeterReading() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getAggregateMeterReading(CLIENT_ID, YEAR);
        assertEquals(YEAR, yearlyEnergyConsumptionDto.getYear());
        assertEquals(780, yearlyEnergyConsumptionDto.getAggregateConsumedEnergy().intValue());

        yearlyEnergyConsumptionDto = electricityMeterReadingsService.getAggregateMeterReading(CLIENT_ID, 2021);
        assertEquals(2021, yearlyEnergyConsumptionDto.getYear().intValue());
        assertEquals(10, yearlyEnergyConsumptionDto.getAggregateConsumedEnergy().intValue());
    }

    @Test
    public void getMeterReadings_noElectricityMeterReadings() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();

        client.setElectricityMeter(electricityMeter);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getMeterReadings(CLIENT_ID, YEAR, null);
        assertEquals(YEAR, yearlyEnergyConsumptionDto.getYear());
        assertEquals(0, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
    }

    @Test
    public void getMeterReadings() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getMeterReadings(CLIENT_ID, YEAR, Optional.ofNullable(null));
        assertEquals(YEAR, yearlyEnergyConsumptionDto.getYear());
        assertEquals(12, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
        assertEquals(1, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getMonth());
        assertEquals(10, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getConsumedEnergy());
        assertEquals(2, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(1).getMonth());
        assertEquals(20, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(1).getConsumedEnergy());
    }

    @Test
    public void getMeterReadings_forMonthAugust() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getMeterReadings(CLIENT_ID, YEAR, Optional.of(8));
        assertEquals(YEAR, yearlyEnergyConsumptionDto.getYear());
        assertEquals(1, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
        assertEquals(8, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getMonth());
        assertEquals(80, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getConsumedEnergy());
    }

    @Test
    public void getMeterReadings_forNotExistingMonth() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getMeterReadings(CLIENT_ID, YEAR, Optional.of(20));
        assertEquals(YEAR, yearlyEnergyConsumptionDto.getYear());
        assertEquals(0, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
    }

    @Test
    public void addMeterReadings() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        final ElectricityMeterReadingModel electricityMeterReadingToAdd = createElectricityMeterReading(electricityMeter, 100L, 2021, Month.DECEMBER, 1200);
        final ElectricityMeterReadingDto electricityMeterReadingDtoToAdd = new ElectricityMeterReadingDto(Month.DECEMBER, 1200);
        electricityMeterReadingDtoToAdd.setYear(2021);
        electricityMeterReadingDtoToAdd.setId(100L);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(electricityMeterReadingRepository.save(any())).thenReturn(electricityMeterReadingToAdd);
        when(electricityMeterReadingModelToDtoConverter.convert(electricityMeterReadingToAdd)).thenReturn(electricityMeterReadingDtoToAdd);

        final ElectricityMeterReadingDto electricityMeterReadingDto = electricityMeterReadingsService.addMeterReading(CLIENT_ID, electricityMeterReadingDtoToAdd);
        assertEquals(100, electricityMeterReadingDto.getId().intValue());
        assertEquals(2021, electricityMeterReadingDto.getYear().intValue());
        assertEquals(12, electricityMeterReadingDto.getMonth().intValue());
        assertEquals(1200, electricityMeterReadingDto.getConsumedEnergy().intValue());
    }

    @Test(expected = ClientNotFoundException.class)
    public void addMeterReadings_clientNotFoundException() {
        final ElectricityMeterReadingDto electricityMeterReadingDtoToAdd = new ElectricityMeterReadingDto(Month.DECEMBER, 1200);
        electricityMeterReadingDtoToAdd.setYear(2021);
        electricityMeterReadingDtoToAdd.setId(100L);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        electricityMeterReadingsService.addMeterReading(CLIENT_ID, electricityMeterReadingDtoToAdd);
    }

    @Test(expected = ElectricityMeterNotFoundException.class)
    public void addMeterReadings_electricityMeterNotFoundException() {
        final ElectricityMeterReadingDto electricityMeterReadingDtoToAdd = new ElectricityMeterReadingDto(Month.DECEMBER, 1200);
        electricityMeterReadingDtoToAdd.setYear(2021);
        electricityMeterReadingDtoToAdd.setId(100L);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        electricityMeterReadingsService.addMeterReading(CLIENT_ID, electricityMeterReadingDtoToAdd);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void addMeterReadings_entityAlreadyExistsException() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        final ElectricityMeterReadingDto electricityMeterReadingDtoToAdd = new ElectricityMeterReadingDto(Month.DECEMBER, 1200);
        electricityMeterReadingDtoToAdd.setYear(2021);
        electricityMeterReadingDtoToAdd.setId(100L);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(electricityMeterReadingRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        electricityMeterReadingsService.addMeterReading(CLIENT_ID, electricityMeterReadingDtoToAdd);
    }

    @Test
    public void updateMeterReadings() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        final ElectricityMeterReadingModel readingToUpdate = createElectricityMeterReading(electricityMeter, 14L, 2021, Month.JANUARY, 1200);

        final ElectricityMeterReadingDto electricityMeterReadingDtoToUpdate = new ElectricityMeterReadingDto(Month.JANUARY, 1200);
        electricityMeterReadingDtoToUpdate.setYear(2021);
        electricityMeterReadingDtoToUpdate.setId(14L);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        electricityMeterReadingsService.updateMeterReading(CLIENT_ID, electricityMeterReadingDtoToUpdate);
        verify(clientRepository, times(1)).findById(CLIENT_ID);
        verify(electricityMeterReadingRepository, times(1)).save(any());
    }

    @Test(expected = ElectricityMeterReadingNotFoundException.class)
    public void updateMeterReadings_readingNotFound() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        final ElectricityMeterReadingModel readingToUpdate = createElectricityMeterReading(electricityMeter, 14L, 2021, Month.JANUARY, 1200);

        final ElectricityMeterReadingDto electricityMeterReadingDtoToUpdate = new ElectricityMeterReadingDto(Month.JANUARY, 1200);
        electricityMeterReadingDtoToUpdate.setYear(1800);
        electricityMeterReadingDtoToUpdate.setId(14L);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        electricityMeterReadingsService.updateMeterReading(CLIENT_ID, electricityMeterReadingDtoToUpdate);
    }

    @Test
    public void deleteMeterReadings() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        final Integer year = 2021;
        final Integer month = 1;
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        electricityMeterReadingsService.deleteMeterReading(CLIENT_ID, year, month);
        verify(clientRepository, times(1)).findById(CLIENT_ID);
        verify(electricityMeterReadingRepository, times(1)).deleteById(14L);
    }

    @Test(expected = ElectricityMeterReadingNotFoundException.class)
    public void deleteMeterReadings_readingsNotFound() {
        final ElectricityMeterModel electricityMeter = createElectricityMeter();
        client.setElectricityMeter(electricityMeter);
        final Integer year = 1800;
        final Integer month = 1;
        electricityMeter.setElectricityMeterReadings(getElectricityMeterReadings(electricityMeter));

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        electricityMeterReadingsService.deleteMeterReading(CLIENT_ID, year, month);
    }

    private List<ElectricityMeterReadingModel> getElectricityMeterReadings(ElectricityMeterModel electricityMeter) {
        final List<ElectricityMeterReadingModel> electricityMeterReadings = new ArrayList<>();
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 1L, 2019, Month.DECEMBER, 120));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 2L, 2020, Month.JANUARY, 10));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 3L, 2020, Month.FEBRUARY, 20));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 4L, 2020, Month.MARCH, 30));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 5L, 2020, Month.APRIL, 40));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 6L, 2020, Month.MAY, 50));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 7L, 2020, Month.JUNE, 60));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 8L, 2020, Month.JULY, 70));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 9L, 2020, Month.AUGUST, 80));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 10L, 2020, Month.SEPTEMBER, 90));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 11L, 2020, Month.OCTOBER, 100));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 12L, 2020, Month.NOVEMBER, 110));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 13L, 2020, Month.DECEMBER, 120));
        electricityMeterReadings.add(createElectricityMeterReading(electricityMeter, 14L, 2021, Month.JANUARY, 10));
        return electricityMeterReadings;
    }

    private ElectricityMeterModel createElectricityMeter() {
        final ElectricityMeterModel electricityMeter = new ElectricityMeterModel();
        electricityMeter.setId(1L);
        electricityMeter.setSerialNumber("123123123");
        return electricityMeter;
    }

    private ElectricityMeterReadingModel createElectricityMeterReading(ElectricityMeterModel electricityMeter, Long id, Integer year, Month month, Integer consumedEnergy) {
        ElectricityMeterReadingModel electricityMeterReading = new ElectricityMeterReadingModel();
        electricityMeterReading.setElectricityMeter(electricityMeter);
        electricityMeterReading.setYear(year);
        electricityMeterReading.setMonth(month);
        electricityMeterReading.setId(id);
        electricityMeterReading.setConsumedEnergy(consumedEnergy);
        return electricityMeterReading;
    }
}
