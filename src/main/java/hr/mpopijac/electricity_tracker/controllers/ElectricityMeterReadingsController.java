package hr.mpopijac.electricity_tracker.controllers;

import hr.mpopijac.electricity_tracker.dto.ElectricityMeterReadingDto;
import hr.mpopijac.electricity_tracker.dto.YearlyEnergyConsumptionDto;
import hr.mpopijac.electricity_tracker.services.ElectricityMeterReadingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static hr.mpopijac.electricity_tracker.controllers.AbstractController.BASE_API_URL;


@RestController
@RequestMapping(BASE_API_URL + "/client/{clientId}/electricity-meter-readings")
public class ElectricityMeterReadingsController extends AbstractController {

    private final ElectricityMeterReadingsService electricityMeterReadingsService;

    @Autowired
    public ElectricityMeterReadingsController(ElectricityMeterReadingsService electricityMeterReadingsService) {
        this.electricityMeterReadingsService = electricityMeterReadingsService;
    }

    @GetMapping("/{year}/aggregate")
    public ResponseEntity<YearlyEnergyConsumptionDto> getAggregateMeterReadingForYear(@PathVariable Long clientId, @PathVariable Integer year) {
        YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getAggregateMeterReading(clientId, year);
        return ResponseEntity.ok(yearlyEnergyConsumptionDto);
    }

    @GetMapping("/{year}/monthly")
    public ResponseEntity<YearlyEnergyConsumptionDto> getMeterReadingsMonthlyForYear(@PathVariable Long clientId,
                                                                                     @PathVariable Integer year,
                                                                                     @RequestParam(required = false) Integer month) {
        YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = electricityMeterReadingsService.getMeterReadings(clientId, year, month);
        return ResponseEntity.ok(yearlyEnergyConsumptionDto);
    }


    @PostMapping(value = "/addMeterReading")
    public ResponseEntity<ElectricityMeterReadingDto> addMeterReading(@PathVariable Long clientId,
                                                                      @RequestBody ElectricityMeterReadingDto meterReading){
        ElectricityMeterReadingDto addedMeterReading = electricityMeterReadingsService.addMeterReading(clientId, meterReading);
        return ResponseEntity.ok(addedMeterReading);
    }
}
