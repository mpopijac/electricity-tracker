package hr.mpopijac.electricity_tracker.controllers;

import hr.mpopijac.electricity_tracker.dto.ElectricityMeterReadingDto;
import hr.mpopijac.electricity_tracker.dto.YearlyEnergyConsumptionDto;
import hr.mpopijac.electricity_tracker.exceptions.ClientNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.ElectricityMeterNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.ElectricityMeterReadingNotFoundException;
import hr.mpopijac.electricity_tracker.exceptions.EntityAlreadyExistsException;
import hr.mpopijac.electricity_tracker.services.ElectricityMeterReadingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static hr.mpopijac.electricity_tracker.controllers.AbstractController.BASE_API_URL;


@RestController
@RequestMapping(BASE_API_URL + "/client/{clientId}/electricity-meter-readings")
public class ElectricityMeterReadingsController extends AbstractController {

    private final ElectricityMeterReadingsService electricityMeterReadingsService;

    @Autowired
    public ElectricityMeterReadingsController(ElectricityMeterReadingsService electricityMeterReadingsService) {
        this.electricityMeterReadingsService = electricityMeterReadingsService;
    }

    /**
     * Get aggregate meter reading for a year
     *
     * @param clientId identifier
     * @param year     fetch data for wanted year
     * @return year and total consumed energy for that year.
     * @throws ClientNotFoundException
     */
    @GetMapping("/{year}/aggregate")
    public ResponseEntity<YearlyEnergyConsumptionDto> getAggregateMeterReadingForYear(@PathVariable Long clientId,
                                                                                      @PathVariable Integer year) {
        YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getAggregateMeterReading(clientId, year);
        return ResponseEntity.ok(yearlyEnergyConsumptionDto);
    }

    /**
     * Get consumed energy for year by months or for specific month
     *
     * @param clientId identifier
     * @param year     for selected year
     * @param month    optional parameter, only for fetching data for specific month
     * @return consumed energy by month
     * @throws ClientNotFoundException
     */
    @GetMapping("/{year}/monthly")
    public ResponseEntity<YearlyEnergyConsumptionDto> getMeterReadingsMonthlyForYear(@PathVariable Long clientId,
                                                                                     @PathVariable Integer year,
                                                                                     @RequestParam(required = false) Optional<Integer> month) {
        YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getMeterReadings(clientId, year, month);
        return ResponseEntity.ok(yearlyEnergyConsumptionDto);
    }

    /**
     * Adding new energy consumption reading for specified client/year/month
     *
     * @param clientId     identifier
     * @param meterReading data to store
     * @return new created record
     * @throws ClientNotFoundException,
     * @throws ElectricityMeterNotFoundException,
     * @throws EntityAlreadyExistsException
     */
    @PostMapping(value = "/addMeterReading")
    public ResponseEntity<ElectricityMeterReadingDto> addMeterReading(@PathVariable Long clientId,
                                                                      @RequestBody ElectricityMeterReadingDto meterReading) {
        ElectricityMeterReadingDto addedMeterReading = electricityMeterReadingsService.addMeterReading(clientId, meterReading);
        return ResponseEntity.ok(addedMeterReading);
    }

    /**
     * Update existing electricity meter reading
     *
     * @param clientId     identifier
     * @param meterReading update data
     * @return only HTTP status 200
     * @throws ClientNotFoundException,
     * @throws ElectricityMeterNotFoundException,
     * @throws ElectricityMeterReadingNotFoundException
     */
    @PutMapping(value = "/updateMeterReading")
    public ResponseEntity<ElectricityMeterReadingDto> updateMeterReading(@PathVariable Long clientId,
                                                                         @RequestBody ElectricityMeterReadingDto meterReading) {
        electricityMeterReadingsService.updateMeterReading(clientId, meterReading);
        return ResponseEntity.ok().build();
    }

    /**
     * Deleting existing electricity meter reading by following parameters:
     *
     * @param clientId identifier
     * @param year     selected
     * @param month    selected
     * @return only HTTP status 200
     * @throws ClientNotFoundException,
     * @throws ElectricityMeterNotFoundException,
     * @throws ElectricityMeterReadingNotFoundException
     */
    @DeleteMapping(value = "/{year}/{month}/")
    public ResponseEntity<ElectricityMeterReadingDto> deleteMeterReading(@PathVariable Long clientId,
                                                                         @PathVariable Integer year,
                                                                         @PathVariable Integer month) {
        electricityMeterReadingsService.deleteMeterReading(clientId, year, month);
        return ResponseEntity.ok().build();
    }
}
