package hr.mpopijac.electricity_tracker.exceptions.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestControllerExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public void exceptionLogging(Exception e, WebRequest request) throws Exception {
        LOG.error(e.getMessage(), e);
        super.handleException(e, request);
    }
}
