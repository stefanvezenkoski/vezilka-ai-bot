package mk.ukim.finki.aibotbackend.web.handler;

import mk.ukim.finki.aibotbackend.web.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns the template's {@code UnsupportedOperationException} stubs into
 * clean HTTP 501 (Not Implemented) responses, so an unimplemented endpoint
 * is clearly distinguishable from a real server error.
 *
 * <p>As you implement the TODO(student) markers, these responses disappear
 * one by one.</p>
 */
@RestControllerAdvice
public class NotImplementedExceptionHandler {
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiError> handleNotImplemented(UnsupportedOperationException exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiError.of(HttpStatus.NOT_IMPLEMENTED, exception.getMessage()));
    }
}
