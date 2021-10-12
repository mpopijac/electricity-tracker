package hr.mpopijac.electricity_tracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ElectricityMeterReadingNotFoundException extends RuntimeException{

    public ElectricityMeterReadingNotFoundException(String message) {
        super(message);
    }
}
