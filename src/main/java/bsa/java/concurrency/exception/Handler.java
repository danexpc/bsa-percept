package bsa.java.concurrency.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
@Log4j2
public class Handler extends ResponseEntityExceptionHandler {

    public static final String DEFAULT_MESSAGE_TEMPLATE = "Exception[{}]: '{}' with cause = {}\n Displaying stack trace:\n{}";

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(InvalidArgumentException e) {
        log.error(DEFAULT_MESSAGE_TEMPLATE, e.getClass(), e.getMessage(), e.getCause() != null ? e.getCause() : "NULL", e.getStackTrace());
        return ResponseEntity
                .unprocessableEntity()
                .body(
                        Map.of(
                                "error", e.getMessage() == null ? "Unprocessable request parameters" : e.getMessage()
                        )
                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error(DEFAULT_MESSAGE_TEMPLATE, e.getClass(), e.getMessage(), e.getCause() != null ? e.getCause() : "NULL", e.getStackTrace());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UnavailableResourceException.class)
    public ResponseEntity<Object> handleUnavailableResourceException(UnavailableResourceException e) {
        log.error(DEFAULT_MESSAGE_TEMPLATE, e.getClass(), e.getMessage(), e.getCause() != null ? e.getCause() : "NULL", e.getStackTrace());
        return ResponseEntity
                .unprocessableEntity()
                .body(
                        Map.of(
                                "error", e.getMessage() == null ? "Failed to load resource" : e.getMessage()
                        )
                );
    }

    @ExceptionHandler(UnsupportedHasherException.class)
    public ResponseEntity<Object> handleUnsupportedHasherException(UnsupportedHasherException e) {
        log.error(DEFAULT_MESSAGE_TEMPLATE, e.getClass(), e.getMessage(), e.getCause() != null ? e.getCause() : "NULL", e.getStackTrace());
        return ResponseEntity
                .unprocessableEntity()
                .body(
                        Map.of(
                                "error", e.getMessage() == null ? "Hasher doesn't exist" : e.getMessage()
                        )
                );
    }
}
